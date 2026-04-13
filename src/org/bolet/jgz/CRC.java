// $Id: CRC.java 96 2007-08-06 10:31:07Z pornin $
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
 * <p>This class contains helper method for computing CRC checksums
 * over some data. The CRC has a 32-bit state, which begins with the
 * "all-ones" value (see the <code>START</code> constant). The CRC
 * result, once all data bytes have been processed, is the bitwise
 * inverse of the state.</p>
 *
 * @version   $Revision: 96 $
 * @author    Thomas Pornin
 */

public final class CRC {

	/**
	 * This class is not meant to be instantiated.
	 */
	private CRC()
	{
	}

	private static final int[] crcTab = new int[256];
	static {
		for (int n = 0; n < 256; n ++) {
			int c = n;
			for (int k = 0; k < 8; k ++) {
				int d = c >>> 1;
				if ((c & 1) != 0)
					d ^= (int)0xEDB88320;
				c = d;
			}
			crcTab[n] = c;
		}
	}

	/**
	 * The initial value for a CRC (this is the "all-one" constant).
	 */
	public static final int START = ~0;

	/**
	 * Update a CRC value with a data byte.
	 *
	 * @param crc   the CRC value
	 * @param v     the data byte (between 0 and 255)
	 * @return  the new CRC value
	 */
	public static int updateCRC(int crc, int v)
	{
		return crcTab[(crc ^ v) & 0xFF] ^ (crc >>> 8);
	}

	/**
	 * Update a CRC value with several data bytes
	 *
	 * @param crc   the CRC value
	 * @param buf   the data bytes
	 * @return  the new CRC value
	 */
	public static int updateCRC(int crc, byte[] buf)
	{
		return updateCRC(crc, buf, 0, buf.length);
	}

	/**
	 * Update a CRC value with several data bytes
	 *
	 * @param crc   the CRC value
	 * @param buf   the data bytes
	 * @param off   the data bytes offset
	 * @param len   the data bytes length (in bytes)
	 * @return  the new CRC value
	 */
	public static int updateCRC(int crc, byte[] buf, int off, int len)
	{
		while (len -- > 0)
			crc = updateCRC(crc, buf[off ++] & 0xFF);
		return crc;
	}
}
