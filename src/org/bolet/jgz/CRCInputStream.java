// $Id: CRCInputStream.java 94 2007-08-06 08:54:24Z pornin $
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
 * This is a kind of filter stream which computes the CRC of read
 * data. It is used internally for checksums over <code>gzip</code>
 * headers.
 *
 * @version   $Revision: 94 $
 * @author    Thomas Pornin
 */

final class CRCInputStream extends InputStream {

	private InputStream sub;
	private int crc;

	/**
	 * Create the filter over the provided stream.
	 *
	 * @param sub   the unerlying stream
	 */
	CRCInputStream(InputStream sub)
	{
		this.sub = sub;
		resetCRC();
	}

	/**
	 * Reset the CRC to its initial value.
	 */
	void resetCRC()
	{
		crc = CRC.START;
	}

	/**
	 * Update the CRC with the provided data byte. This is used
	 * to process some bytes which were received on the stream
	 * before instantiating this object.
	 *
	 * @param v   the data byte (betweem 0 and 255)
	 */
	void updateCRC(int v)
	{
		crc = CRC.updateCRC(crc, v);
	}

	/**
	 * Get the current CRC (bitwise negation applied).
	 *
	 * @return  the current CRC
	 */
	int getCRC()
	{
		return ~crc;
	}

	/** @see InputStream */
	public int read()
		throws IOException
	{
		int v = sub.read();
		if (v >= 0)
			updateCRC(v);
		return v;
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
		int rlen = sub.read(buf, off, len);
		if (rlen > 0)
			crc = CRC.updateCRC(crc, buf, off, rlen);
		return rlen;
	}
}
