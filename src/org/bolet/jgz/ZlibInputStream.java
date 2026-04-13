// $Id: ZlibInputStream.java 118 2007-08-11 14:37:14Z pornin $
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

/**
 * <p>This class implements a decoder stream for a compressed data
 * stream in <code>zlib</code> format (RFC 1950).</p>
 *
 * <p>Such a stream is self-terminated; <code>read()</code> requests
 * will return -1 if the logical data end has been reached, regardless
 * of the status of the underlying stream, which may contain more
 * data. <code>Zlib</code> streams have an internal 32-bit checksum,
 * which this implementation verifies when the stream logical end is
 * reached (a read attempt beyond the end is enough to trigger this
 * verification; calling <code>close()</code> also works).</p>
 *
 * <p><code>Zlib</code> streams may use a dictionary, which is a bunch
 * of uncompressed data that the compressor may use for backward
 * references, as if it has been compressed and transmitted beforehand.
 * The checksum is <emph>not</emph> computed over the dictionary.
 * However, for indexing purposes, the checksum of the dictionary is
 * provided in the stream. Whether a dictionary must be used or not is
 * indicated in the stream. This class does <emph>not</emph> check that
 * a dictionary is indeed provided as requested. It does not check
 * either whether the provided dictionary matches the checksum which is
 * indicated in the stream header.</p>
 *
 * <p>The stream may be initialized with the <code>partial</code> flag
 * set; in that mode, <code>read()</code> requests mark a stop at the
 * end of each deflate block. More precisely, <code>read(byte[])</code>
 * and <code>read(byte[],int,int)</code> normally process input data
 * until the logical stream end or the output buffer size, whichever
 * comes first; in <code>partial</code> mode, these functions will
 * return bytes from only one deflate block at most. This allows the
 * calling application to implement stateful interruptions, as is
 * required for some streaming protocols (e.g. SSH). A consequence is
 * that <code>read(byte[])</code> and <code>read(byte[],int,int)</code>
 * may return 0, if the next block happens to be empty
 * (<strong>warning:</strong> this is in violation of the
 * <code>InputStream</code> API).</p>
 *
 * @version   $Revision: 118 $
 * @author    Thomas Pornin
 */

public class ZlibInputStream extends InputStream {

	/** Compression method: the "deflate" algorithm (RFC 1951). */
	public static final int DEFLATE = 8;

	private InputStream sub;
	private boolean partial;
	private Inflater inflater;
	private byte[] oneByte;
	private int compressionMethod;
	private int compressionInfo;
	private int compressionLevel;
	private boolean hasDictionary;
	private int dictID;
	private Adler32 adler;
	private boolean eofReached;

	/**
	 * Create the stream. The <code>zlib</code> header is
	 * immediately read and decoded. This constructor is equivalent
	 * to the two-parameter constructor with the
	 * <code>partial</code> parameter set to <code>false</code>.
	 *
	 * @param sub       the underlying input stream
	 * @throws IOException  on I/O or format error
	 */
	public ZlibInputStream(InputStream sub)
		throws IOException
	{
		this(sub, false);
	}

	/**
	 * Create the stream. The <code>zlib</code> header is
	 * immediately read and decoded. Note: if partial mode is
	 * enabled, then the <code>read()</code> methods may return 0
	 * (although some input data will be consumed in the process).
	 *
	 * @param sub       the underlying input stream
	 * @param partial   <code>true</code> to use partial mode
	 * @throws IOException  on I/O or format error
	 */
	public ZlibInputStream(InputStream sub, boolean partial)
		throws IOException
	{
		this.sub = sub;
		this.partial = partial;

		/*
		 * Read header.
		 */
		int cmf = read1();
		int flg = read1();
		if (((cmf << 8) + flg) % 31 != 0)
			throw new JGZException("invalid Zlib stream header");
		compressionMethod = cmf & 0x0F;
		if (compressionMethod != DEFLATE)
			throw new JGZException("unknown compression method: "
				+ compressionMethod);
		compressionInfo = (cmf >>> 4);
		if (compressionInfo > 7)
			throw new JGZException("invalid deflate window size: "
				+ compressionInfo);
		compressionLevel = (flg >>> 6);
		hasDictionary = (flg & 0x20) != 0;
		adler = new Adler32();
		if (hasDictionary)
			dictID = reads4();

		/*
		 * Initialize inflater.
		 */
		inflater = new Inflater();
		inflater.reset(sub);
		oneByte = new byte[1];
		eofReached = false;
	}

	/**
	 * Read one data byte.
	 *
	 * @return  the byte value (between 0 and 255)
	 * @throws IOException  on I/O error (including EOF)
	 */
	private int read1()
		throws IOException
	{
		int x = sub.read();
		if (x < 0)
			throw new JGZException("unexpected EOF");
		return x;
	}

	/**
	 * Read a 32-bit value, big-endian.
	 *
	 * @return  the value
	 * @throws IOException  on I/O error (including EOF)
	 */
	private int reads4()
		throws IOException
	{
		int v3 = read1();
		int v2 = read1();
		int v1 = read1();
		int v0 = read1();
		return (v3 << 24) | (v2 << 16) | (v1 << 8) | v0;
	}

	/**
	 * Get the compression method (normally <code>DEFLATE</code>).
	 *
	 * @return  the compression method
	 */
	public int getCompressionMethod()
	{
		return compressionMethod;
	}

	/**
	 * Get the compression information. This is a 4-bit value whose
	 * meaning depends on the compression method. For the "deflate"
	 * method, if that value is <code>n</code>, then the backward
	 * sequence references may reach up to <code>2^(n+8)</code>
	 * previously uncompressed bytes; the maximum standard value for
	 * <code>n</code> is then 7 (which implies a 32 kB window).
	 *
	 * @return  the compression information
	 */
	public int getCompressionInfo()
	{
		return compressionInfo;
	}

	/**
	 * Get the compression level. This is a two-bit value which
	 * qualifies the effort deployed by the compressor. It has
	 * no strictly defined semantics and is ignored by this
	 * implementation.
	 *
	 * @return  the compression level
	 */
	public int getCompressionLevel()
	{
		return compressionLevel;
	}

	/**
	 * Tell whether a dictionary was used by the compressor. If this
	 * method returns <code>true</code>, then the same dictionary
	 * must be input; the dictionary identifier can be retrieved
	 * with the <code>getDictionaryId()</code> method.
	 *
	 * @return  <code>true</code> for a dictionary
	 */
	public boolean hasDictionary()
	{
		return hasDictionary;
	}

	/**
	 * Get the dictionary identifier; if none was provided, then 0
	 * is returned (but a valid dictionary could also use the
	 * identifier value of 0; use <code>hasDictionary()</code> to
	 * know if a dictionary ID was supplied in the stream).
	 *
	 * @return  the dictionary identifier
	 */
	public int getDictionaryId()
	{
		return dictID;
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
		inflater.processDictionary(dict);
	}

	/**
	 * Process a dictionary: the buffer contents are used as
	 * dictionary.
	 *
	 * @param dict   the dictionary
	 */
	public void processDictionary(byte[] dict)
	{
		inflater.processDictionary(dict);
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
		inflater.processDictionary(dict, off, len);
	}

	/**
	 * Change the raw stream used to get compressed data bytes.
	 * The state is not changed.
	 *
	 * @param sub   the new input stream
	 */
	public void setRawStream(InputStream sub)
	{
		this.sub = sub;
		inflater.setRawStream(sub);
	}

	/**
	 * Get the final checksum and verify it, unless this job was
	 * already done.
	 *
	 * @throws IOException  on I/O or format error
	 */
	private void checkEnd()
		throws IOException
	{
		if (!eofReached) {
			int sum = adler.getSum();
			int val = reads4();
			if (sum != val)
				throw new JGZException("incorrect checksum");
			eofReached = true;
		}
	}

	/**
	 * This <code>close()</code> method checks that the logical
	 * end-of-stream has been reached; the checksum is verified if
	 * this has not already been done. Moreover, the underlying
	 * stream is also closed.
	 *
	 * @see InputStream
	 */
	public void close()
		throws IOException
	{
		if (!eofReached) {
			if (read() != -1)
				throw new JGZException(
					"trailing garbage in zlib stream");
		}
		sub.close();
	}

	/** @see InputStream */
	public int read()
		throws IOException
	{
		if (read(oneByte, 0, 1) != 1)
			return -1;
		return oneByte[0] & 0xFF;
	}

	/** @see InputStream */
	public int read(byte[] buf)
		throws IOException
	{
		return read(buf, 0, buf.length);
	}

	/** @see InputStream */
	public int read(byte[] buf, int off, int len)
		throws IOException
	{
		if (len == 0)
			return 0;
		if (partial) {
			boolean first = true;
			for (;;) {
				int rlen = inflater.readBlock(buf, off, len);
				if (rlen > 0) {
					adler.update(buf, off, rlen);
					return rlen;
				}
				if (!first)
					return 0;
				first = false;
				if (!inflater.nextBlock()) {
					checkEnd();
					return -1;
				}
			}
		} else {
			int rlen = inflater.readAll(buf, off, len);
			if (rlen > 0)
				adler.update(buf, off, rlen);
			else
				rlen = -1;
			if (rlen < len)
				checkEnd();
			return rlen;
		}
	}
}
