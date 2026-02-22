// $Id: ZlibOutputStream.java 105 2007-08-07 07:45:45Z pornin $
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
 * <p>This class implements a stream which compresses data into the
 * <code>zlib</code> format (RFC 1950).</p>
 *
 * <p>The compression level can be specified, as a symbolic value
 * identical to what the <code>Deflater</code> class expects. The
 * default compression level is <code>MEDIUM</code>.</p>
 *
 * @version   $Revision: 105 $
 * @author    Thomas Pornin
 */

public class ZlibOutputStream extends OutputStream {

	private OutputStream out;
	private Deflater deflater;
	private byte[] oneByte;
	private Adler32 adler;
	private int level;
	private boolean hasDictionary;
	private int dictID;
	private boolean headerWritten;

	/**
	 * Create the stream with the provided transport stream. The
	 * default compression level (<code>MEDIUM</code>) is used.
	 *
	 * @param out   the transport stream
	 * @throws IOException  on I/O error with the transport stream
	 */
	public ZlibOutputStream(OutputStream out)
		throws IOException
	{
		this(out, Deflater.MEDIUM);
	}

	/**
	 * Create the stream with the provided transport stream. The
	 * provided compression level is used.
	 *
	 * @param out     the transport stream
	 * @param level   the compression level
	 * @throws IOException  on I/O error with the transport stream
	 */
	public ZlibOutputStream(OutputStream out, int level)
		throws IOException
	{
		this.out = out;
		this.level = level;
		deflater = new Deflater(level);
		deflater.setOut(out);
		oneByte = new byte[1];
		adler = new Adler32();
	}

	/**
	 * Process a dictionary: the stream contents are used as
	 * dictionary.
	 *
	 * @param dict   the dictionary stream
	 * @throws IOException  on I/O error with <code>dict</code>
	 */
	public void processDictionary(InputStream dict)
		throws IOException
	{
		byte[] buf = new byte[4096];
		Adler32 da = new Adler32();
		for (;;) {
			int rlen = dict.read(buf);
			if (rlen < 0)
				break;
			deflater.processDictionary(buf, 0, rlen);
			da.update(buf, 0, rlen);
		}
		hasDictionary = true;
		dictID = da.getSum();
	}

	/**
	 * Process a dictionary: the buffer contents are used as
	 * dictionary.
	 *
	 * @param dict   the dictionary
	 */
	public void processDictionary(byte[] dict)
	{
		processDictionary(dict, 0, dict.length);
	}

	/**
	 * Process a dictionary: the buffer contents are used as
	 * dictionary.
	 *
	 * @param dict   the dictionary
	 * @param off    the dictionary offset
	 * @param len    the dictionary length
	 */
	public void processDictionary(byte[] dict, int off, int len)
	{
		deflater.processDictionary(dict, off, len);
		hasDictionary = true;
		Adler32 da = new Adler32();
		da.update(dict, off, len);
		dictID = da.getSum();
	}

	/**
	 * Write out a 32-bit integer on the stream (big-endian).
	 *
	 * @param v   the value
	 * @throws IOException  on I/O error with the transport stream
	 */
	private void writes4(int v)
		throws IOException
	{
		out.write(v >>> 24);
		out.write(v >>> 16);
		out.write(v >>> 8);
		out.write(v);
	}

	/**
	 * Write out the header, if needed.
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	private void writeHeader()
		throws IOException
	{
		if (headerWritten)
			return;

		/*
		 * Compression method = 8 (DEFLATE).
		 * Compression info = 7 (window length = 2^15).
		 */
		int cmf = 8 + (7 << 4);

		/*
		 * Flags.
		 */
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

		out.write(cmf);
		out.write(flags);
		if (hasDictionary)
			writes4(dictID);
		headerWritten = true;
	}

	/**
	 * Close this stream; the transport stream is also closed.
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void close()
		throws IOException
	{
		terminate();
		out.close();
	}

	/**
	 * Close the compression stream but do <strong>not</strong>
	 * close the transport stream. All bytes which constitute the
	 * <code>zlib</code> file are written on the transport stream
	 * (but the transport stream itself is not flushed).
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void terminate()
		throws IOException
	{
		writeHeader();
		deflater.terminate();
		writes4(adler.getSum());
	}

	/**
	 * Flush this stream; the transport stream is also flushed. At
	 * the DEFLATE level, a "sync flush" is performed, which ensures
	 * that output bytes written so far on the transport stream are
	 * sufficient to recover all the input bytes currently processed.
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void flush()
		throws IOException
	{
		flushSync(true);
	}

	/**
	 * Flush this stream; the transport stream is also flushed. At
	 * the DEFLATE level, a "sync flush" is performed, with the
	 * provided value for <code>withData</code>.
	 *
	 * @param withData   <code>false</code> to omit the 00 00 FF FF bytes
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void flushSync(boolean withData)
		throws IOException
	{
		writeHeader();
		deflater.flushSync(withData);
		deflater.getOut().flush();
	}

	/**
	 * Flush this stream; the transport stream is also flushed. At
	 * the DEFLATE level, a "full flush" is performed, with the
	 * provided value for <code>withData</code>.
	 *
	 * @param withData   <code>false</code> to omit the 00 00 FF FF bytes
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void flushFull(boolean withData)
		throws IOException
	{
		writeHeader();
		deflater.flushFull(withData);
		deflater.getOut().flush();
	}

	/**
	 * Flush this stream; the transport stream is also flushed. At
	 * the DEFLATE level, a "partial flush" is performed.
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void flushPartial()
		throws IOException
	{
		writeHeader();
		deflater.flushPartial();
		deflater.getOut().flush();
	}

	/** @see OutputStream */
	public void write(int b)
		throws IOException
	{
		oneByte[0] = (byte)b;
		write(oneByte, 0, 1);
	}

	/** @see OutputStream */
	public void write(byte[] buf)
		throws IOException
	{
		write(buf, 0, buf.length);
	}

	/** @see OutputStream */
	public void write(byte[] buf, int off, int len)
		throws IOException
	{
		writeHeader();
		deflater.process(buf, off, len);
		adler.update(buf, off, len);
	}
}
