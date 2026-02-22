// $Id: Adler32.java 114 2007-08-10 12:20:03Z pornin $
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

/**
 * <p>This class can be used to compute Adler-32 checksums (specified
 * in RFC 1950 and used in <code>zlib</code> streams).</p>
 *
 * @version   $Revision: 114 $
 * @author    Thomas Pornin
 */

public final class Adler32 {

	private int s1, s2;

	/**
	 * Create the checksum engine with the standard initial state.
	 */
	public Adler32()
	{
		init();
	}

	/**
	 * Reset the engine to the standard initial state.
	 */
	public void init()
	{
		s1 = 1;
		s2 = 0;
	}

	/**
	 * Set the current checksum value.
	 *
	 * @param val   the new checksum state
	 */
	public void init(int val)
	{
		s1 = val & 0xFFFF;
		s2 = val >>> 16;
	}

	/**
	 * Update the cheksum with the provided input byte.
	 *
	 * @param v   the input byte (between 0 and 255)
	 */
	public void update(int v)
	{
		s1 += v;
		if (s1 >= 65521)
			s1 -= 65521;
		s2 += s1;
		if (s2 >= 65521)
			s2 -= 65521;
	}

	/**
	 * Update the cheksum with the provided input bytes.
	 *
	 * @param buf   the data buffer
	 */
	public void update(byte[] buf)
	{
		update(buf, 0, buf.length);
	}

	/**
	 * Update the cheksum with the provided input bytes.
	 *
	 * @param buf   the data buffer
	 * @param off   the data offset
	 * @param len   the data length (in bytes)
	 */
	public void update(byte[] buf, int off, int len)
	{
		while (len -- > 0)
			update(buf[off ++] & 0xFF);
	}

	/**
	 * Get the current checksum value. This does not invalidate the
	 * state.
	 *
	 * @return  the current checksum
	 */
	public int getSum()
	{
		return s1 + (s2 << 16);
	}
}
