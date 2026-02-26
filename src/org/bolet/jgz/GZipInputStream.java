// $Id: GZipInputStream.java 122 2007-08-18 08:25:04Z pornin $
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
 * <p>This class decompresses a data stream compressed in the
 * <code>gzip</code> format, as documented in RFC 1952. It is somewhat
 * similar to the standard <code>java.util.zip.GZIPInputStream</code>
 * class.</p>
 *
 * <p>This stream is <strong>blocking</strong>: when asked to read a
 * given amount of uncompressed bytes, it will return that number of
 * bytes, possibly invoking many times the underlying stream. A short
 * count is possible only if the end of the underlying stream was
 * reached. Note that end-of-stream may occur only between
 * <code>gzip</code> "members"; unexpected end-of-stream is a format
 * error which is reported as such.</p>
 *
 * <p><code>gzip</code> streams may contain some meta-information such
 * as a file name or producer OS; these are not returned by this class.
 * To access meta-information, use the more generic
 * <code>GZipDecoder</code> class.</p>
 *
 * <p>It is <strong>highly recommended</strong> that the provided source
 * input stream is buffered. The inflater engine will read it byte by
 * byte most of the time.</p>
 *
 * @version   $Revision: 122 $
 * @author    Thomas Pornin
 */

public class GZipInputStream extends InputStream {

	private GZipDecoder gd;
	private boolean needMember;
	private Inflater inflater;
	private byte[] oneByte;
	private int uncompressedSize, uncompressedCRC;

	/**
	 * Create the stream over the provided underlying stream.
	 *
	 * @param sub   the underlying stream
	 */
	public GZipInputStream(InputStream sub)
	{
		gd = new GZipDecoder(sub);
		oneByte = new byte[1];
		needMember = true;
		inflater = new Inflater();
	}

	/**
	 * The implementation for this method closes the underlying
	 * input stream.
	 *
	 * @see InputStream
	 */
	public void close()
		throws IOException
	{
		gd.getSubStream().close();
	}

	/** @see InputStream */
	public int read()
		throws IOException
	{
		int r = read(oneByte, 0, 1);
		if (r < 0)
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
		int rlen = 0;
		while (len > 0) {
			if (needMember) {
				InputStream in = gd.nextMember();
				if (in == null)
					return rlen == 0 ? -1 : rlen;
				if (gd.getCompressionMethod()
					!= GZipDecoder.DEFLATE)
					throw new JGZException(
						"unknown compression method");
				inflater.reset(in);
				uncompressedSize = 0;
				uncompressedCRC = CRC.START;
				needMember = false;
			}
			int vlen = inflater.readAll(buf, off, len);
			if (vlen > 0) {
				uncompressedSize += vlen;
				uncompressedCRC = CRC.updateCRC(
					uncompressedCRC, buf, off, vlen);
				return vlen;
			}
			gd.closeMember();
			if (uncompressedSize != gd.getUncompressedSize())
				throw new JGZException(
					"decompressed size mismatch");
			if (~uncompressedCRC != gd.getUncompressedCRC())
				throw new JGZException(
					"CRC value mismatch");
			needMember = true;
		}
		return rlen > 0 ? rlen : -1;
	}
}
