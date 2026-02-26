// $Id$
/*
 * Copyright (c) 2007  Thomas Pornin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bolet.jgz;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>This class implements a DEFLATE engine as a state machine
 * (deflation process). The machine consumes uncompressed data and
 * outputs compressed data; none of the calls to this class ever
 * blocks.</p>
 *
 * <p>At any time, the machine has some input data to process, and
 * an optional scheduled flush or terminate action, to be performed
 * when the current input data is completely processed. Only a single
 * action may be scheduled at a time. When the current input data is
 * finished, new data can be input with <code>setInput()</code>. The
 * provided input data is NOT copied internally, hence there is no
 * problem in using large buffers.</p>
 *
 * @version   $Revision$
 * @author    Thomas Pornin
 */

public final class DeflaterStateMachine {

	/*
	 * Internally, we use the fact that the deflater implementation
	 * will not output more than one block for each 16384 input
	 * bytes. The size of that block will be, at worst, 65537 bytes
	 * (including the three-bit block header). We must also add some
	 * extra bytes so as to accomodate the flush operations, which
	 * may trigger a few extra blocks.
	 */

	private Deflater deflater;
	private int level, windowBits;
	private boolean raw;

	private byte[] input;
	private int inputOff, inputLen;
	private BufferedOutput bufo;
	private boolean finished;
	private int todo;
	private boolean hasDictionary;
	private int dictID;
	private boolean needHeader, needFooter;
	private Adler32 adler;

	private static final int TODO_NONE           = 0;
	private static final int TODO_FINISH         = 1;
	private static final int TODO_FLUSH_PARTIAL  = 2;
	private static final int TODO_FLUSH_SYNC     = 3;
	private static final int TODO_FLUSH_SYNC_ND  = 4;
	private static final int TODO_FLUSH_FULL     = 5;
	private static final int TODO_FLUSH_FULL_ND  = 6;

	/**
	 * Build the state machine instance with default compression
	 * parameters. If <code>raw</code> is <code>false</code>, then
	 * the output data will use the zlib format (RFC 1950);
	 * otherwise, it will consist of only the raw sequence of
	 * compressed blocks.
	 *
	 * @param raw   <code>true</code> to omit zlib header and footer
	 */
	public DeflaterStateMachine(boolean raw)
	{
		this(Deflater.MEDIUM, 15, raw);
	}

	/**
	 * Build the state machine instance with the provided compression
	 * parameters. If <code>raw</code> is <code>false</code>, then
	 * the output data will use the zlib format (RFC 1950);
	 * otherwise, it will consist of only the raw sequence of
	 * compressed blocks.
	 *
	 * @param level        the compression strategy (1 to 4)
	 * @param windowBits   the DEFLATE window bit size (9 to 15)
	 * @param raw          <code>true</code> to omit zlib header and footer
	 */
	public DeflaterStateMachine(int level, int windowBits, boolean raw)
	{
		deflater = new Deflater(level, windowBits);
		this.level = level;
		this.windowBits = windowBits;
		this.raw = raw;
		bufo = new BufferedOutput();
		deflater.setOut(bufo);
		needHeader = !raw;
		needFooter = !raw;
		adler = new Adler32();
	}

	/**
	 * Reset the machine to its initial state.
	 */
	public void reset()
	{
		deflater.reset();
		input = null;
		inputOff = 0;
		inputLen = 0;
		bufo.reset();
		finished = false;
		todo = TODO_NONE;
		hasDictionary = false;
		needHeader = !raw;
		needFooter = !raw;
		adler.init();
	}

	/**
	 * Test whether some fresh input data is necessary. If this
	 * method returns <code>true</code>, then it is guaranteed that
	 * <code>deflate()</code> will return 0 until some new input
	 * data is added. When <code>true</code> is returned, it is
	 * guaranteed that all previously input data has been processed,
	 * which means that <code>setInput()</code> can be called safely.
	 *
	 * @return  <code>true</code> when some input is needed
	 */
	public boolean needsInput()
	{
		return !finished && bufo.isEmpty()
			&& inputLen == 0 && todo == TODO_NONE;
	}

	/**
	 * Set the dictionary. When using zlib format (RFC 1950), the
	 * dictionary must be set before the first call to
	 * <code>deflate()</code>.
	 *
	 * @param dict   the dictionary
	 */
	public void setDictionary(byte[] dict)
	{
		setDictionary(dict, 0, dict.length);
	}

	/**
	 * Set the dictionary. When using zlib format (RFC 1950), the
	 * dictionary must be set before the first call to
	 * <code>deflate()</code>.
	 *
	 * @param dict   the dictionary
	 * @param off    the dictionary offset
	 * @param len    the dictionary length
	 */
	public void setDictionary(byte[] dict, int off, int len)
	{
		Adler32 da = new Adler32();
		da.update(dict, off, len);
		deflater.processDictionary(dict, off, len);
		dictID = da.getSum();
		hasDictionary = true;
	}

	/**
	 * Insert new input data. This call discards any previously
	 * inserted data which was not already processed. The provided
	 * array is linked in, and must not be modified until either all
	 * data was processed, or a new input buffer has been provided.
	 *
	 * @param buf   the new input data
	 */
	public void setInput(byte[] buf)
	{
		setInput(buf, 0, buf.length);
	}

	/**
	 * Insert new input data. This call discards any previously
	 * inserted data which was not already processed. The provided
	 * array is linked in, and must not be modified until either all
	 * data was processed, or a new input buffer has been provided.
	 *
	 * @param buf   the new input data
	 * @param off   the new input data offset
	 * @param len   the new input data length
	 */
	public void setInput(byte[] buf, int off, int len)
	{
		input = buf;
		inputOff = off;
		inputLen = len;
	}

	private void setTodo(int action)
	{
		if (todo != TODO_NONE)
			throw new IllegalArgumentException(
				"an action is already scheduled");
		todo = action;
	}

	private void done()
	{
		todo = TODO_NONE;
	}

	/**
	 * Finish the stream: the output will be marked as closed at
	 * the end of the currently provided input.
	 */
	public void finish()
	{
		setTodo(TODO_FINISH);
	}

	/**
	 * Flush the stream: the output will be flushed (partial flush)
	 * at the end of the currently provided input.
	 */
	public void flushPartial()
	{
		setTodo(TODO_FLUSH_PARTIAL);
	}

	/**
	 * Flush the stream: the output will be flushed (sync flush)
	 * at the end of the currently provided input.
	 *
	 * @param withData   <code>false</code> to omit the 00 00 FF FF bytes
	 */
	public void flushSync(boolean withData)
	{
		setTodo(withData ? TODO_FLUSH_SYNC : TODO_FLUSH_SYNC_ND);
	}

	/**
	 * Flush the stream: the output will be flushed (full flush)
	 * at the end of the currently provided input.
	 *
	 * @param withData   <code>false</code> to omit the 00 00 FF FF bytes
	 */
	public void flushFull(boolean withData)
	{
		setTodo(withData ? TODO_FLUSH_FULL : TODO_FLUSH_FULL_ND);
	}

	/**
	 * Test whether the compression run is finished. If this method
	 * returns <code>true</code>, then the machine will not require
	 * any new input and will not output bytes anymore.
	 *
	 * @return  <code>true</code> on a finished machine
	 */
	public boolean finished()
	{
		return finished && bufo.isEmpty();
	}

	private void writeHeader()
	{
		if (!needHeader)
			return;

		int cmf = 8 + ((windowBits - 8) << 4);
		int flags;
		switch (level) {
		case Deflater.HUFF:
			flags = 0;
			break;
		case Deflater.SPEED:
			flags = 1;
			break;
		case Deflater.MEDIUM:
			flags = 2;
			break;
		case Deflater.COMPACT:
			flags = 3;
			break;
		default:
			flags = 2;
			break;
		}
		flags <<= 6;
		if (hasDictionary)
			flags |= (1 << 5);
		int rem = ((cmf << 8) | flags) % 31;
		if (rem > 0)
			flags += (31 - rem);
		bufo.write(cmf);
		bufo.write(flags);
		if (hasDictionary) {
			bufo.write(dictID >>> 24);
			bufo.write(dictID >>> 16);
			bufo.write(dictID >>> 8);
			bufo.write(dictID);
		}

		needHeader = false;
	}

	private void writeFooter()
	{
		if (!needFooter)
			return;

		int sum = adler.getSum();
		bufo.write(sum >>> 24);
		bufo.write(sum >>> 16);
		bufo.write(sum >>> 8);
		bufo.write(sum);

		needFooter = false;
	}

	/**
	 * Perform the end-of-input scheduled action. If there was such
	 * an action, then <code>true</code> is returned.
	 *
	 * @return  <code>false</code> if there was no action
	 * @throws IOException  (never)
	 */
	private boolean performAction()
		throws IOException
	{
		switch (todo) {
		case TODO_FINISH:
			deflater.terminate();
			writeFooter();
			finished = true;
			break;
		case TODO_FLUSH_PARTIAL:
			deflater.flushPartial();
			break;
		case TODO_FLUSH_SYNC:
			deflater.flushSync(true);
			break;
		case TODO_FLUSH_SYNC_ND:
			deflater.flushSync(false);
			break;
		case TODO_FLUSH_FULL:
			deflater.flushFull(true);
			break;
		case TODO_FLUSH_FULL_ND:
			deflater.flushFull(false);
			break;
		default:
			return false;
		}
		done();
		return true;
	}

	/**
	 * Get some compressed data bytes. The number of actually
	 * produced compressed bytes is returned; this is 0 if either
	 * the buffer has size 0, or there is no data to return.
	 *
	 * @param buf   the compressed data buffer
	 * @return  the number of returned compressed bytes
	 */
	public int deflate(byte[] buf)
	{
		return deflate(buf, 0, buf.length);
	}

	/**
	 * Get some compressed data bytes. The number of actually
	 * produced compressed bytes is returned; this is 0 if either
	 * the buffer has size 0, or there is no data to return.
	 *
	 * @param buf   the compressed data buffer
	 * @param off   the compressed data buffer offset
	 * @param len   the compressed data buffer length
	 * @return  the number of returned compressed bytes
	 */
	public int deflate(byte[] buf, int off, int len)
	{
		if (len == 0)
			return 0;
		if (needHeader)
			writeHeader();

		if (finished)
			return bufo.emptyInto(buf, off, len);

		try {
			for (;;) {
				int r = bufo.emptyInto(buf, off, len);
				if (r > 0)
					return r;
				int ilen = inputLen;
				if (ilen == 0) {
					if (!performAction())
						return 0;
					return bufo.emptyInto(buf, off, len);
				}
				if (ilen > 16384)
					ilen = 16384;
				deflater.process(input, inputOff, ilen);
				adler.update(input, inputOff, ilen);
				inputOff += ilen;
				inputLen -= ilen;
			}
		} catch (IOException ioe) {
			// impossible
			throw new Error(ioe.getMessage());
		}
	}

	/**
	 * Get some compressed data bytes. The number of actually
	 * produced compressed bytes is returned; this is 0 if there
	 * is no data to return. When this method is called, all the
	 * current input is processed, and the compressed data is
	 * written into the <code>out</code> stream.
	 *
	 * @param out   the compressed data destination
	 * @return  the number of returned compressed bytes
	 * @throws IOException  on I/O error with the output stream
	 */
	public int deflate(OutputStream out)
		throws IOException
	{
		if (needHeader)
			writeHeader();
		if (finished)
			return bufo.emptyInto(out);

		int total = 0;
		for (;;) {
			int r = bufo.emptyInto(out);
			total += r;
			int ilen = inputLen;
			if (ilen == 0) {
				if (!performAction())
					return total;
				return total + bufo.emptyInto(out);
			}
			if (ilen > 16384)
				ilen = 16384;
			deflater.process(input, inputOff, ilen);
			adler.update(input, inputOff, ilen);
			inputOff += ilen;
			inputLen -= ilen;
		}
	}

	/**
	 * The buffer size. This should be slightly greater than 65537.
	 */
	private static final int BUFLEN = 65600;

	private static final class BufferedOutput extends OutputStream {

		private byte[] buffer = new byte[BUFLEN];
		private int begin, end;

		/** @see OutputStream */
		public void write(int v)
		{
			buffer[end ++] = (byte)v;
			if (end == BUFLEN)
				end = 0;

			/* DEBUG */
			if (end == begin)
				throw new Error("buffer overrun");
		}

		/** @see OutputStream */
		public void write(byte[] buf)
		{
			write(buf, 0, buf.length);
		}

		/** @see OutputStream */
		public void write(byte[] buf, int off, int len)
		{
			/* DEBUG */
			int stored = end - begin;
			if (stored < 0)
				stored += BUFLEN;
			if ((stored + len) >= BUFLEN)
				throw new Error("buffer overrun");

			int rem = BUFLEN - end;
			if (len <= rem) {
				System.arraycopy(buf, off, buffer, end, len);
				end += len;
				if (end == BUFLEN)
					end = 0;
			} else {
				System.arraycopy(buf, off, buffer, end, rem);
				System.arraycopy(buf, off + rem,
					buffer, 0, len - rem);
				end = len - rem;
			}
		}

		/**
		 * Transfer some of the buffered output into the
		 * provided buffer.
		 *
		 * @param buf   the destination buffer
		 * @param off   the destination offset
		 * @param len   the maximum number of bytes to transfer
		 * @return  the number of bytes actually transfered
		 */
		int emptyInto(byte[] buf, int off, int len)
		{
			if (end == begin)
				return 0;

			if (end > begin) {
				int clen = end - begin;
				if (clen > len)
					clen = len;
				System.arraycopy(buffer, begin, buf, off, clen);
				begin += clen;
				return clen;
			}

			int c1 = BUFLEN - begin;
			if (c1 >= len) {
				System.arraycopy(buffer, begin, buf, off, len);
				begin += len;
				if (begin == BUFLEN)
					begin = 0;
				return len;
			}

			System.arraycopy(buffer, begin, buf, off, c1);
			begin = 0;
			return c1 + emptyInto(buf, off + c1, len - c1);
		}

		/**
		 * Write all currently buffered output data on the
		 * provided stream.
		 *
		 * @param out   the output stream
		 * @return  the number of bytes written
		 * @throws IOException  on I/O error with the stream
		 */
		int emptyInto(OutputStream out)
			throws IOException
		{
			if (begin == end)
				return 0;

			int r;
			if (end > begin) {
				r = end - begin;
				out.write(buffer, begin, r);
			} else {
				r = (BUFLEN + end) - begin;
				out.write(buffer, begin, BUFLEN - begin);
				out.write(buffer, 0, end);
			}
			begin = end;
			return r;
		}

		/**
		 * Test whether this buffer is empty.
		 *
		 * @return  <code>true</code> for an empty buffer
		 */
		boolean isEmpty()
		{
			return begin == end;
		}

		/**
		 * Reset the buffer to its initial state (empty).
		 */
		void reset()
		{
			begin = 0;
			end = 0;
		}
	}
}
