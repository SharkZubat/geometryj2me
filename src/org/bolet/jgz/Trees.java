// $Id: Trees.java 117 2007-08-11 14:36:53Z pornin $
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
 * This class contains some helper code related to Huffman codes.
 *
 * @version   $Revision: 117 $
 * @author    Thomas Pornin
 */

final class Trees {

	/**
	 * <code>LENGTH[n]</code> contains the sequence copy length
	 * when the symbol <code>257+n</code> has been read. The actual
	 * copy length may be augmented with a value from some extra bits.
	 */
	static final int[] LENGTH;

	/**
	 * <code>LENGTH_ENUM[n]</code> contains the number of extra bits
	 * which shall be read, in order to augment the sequence copy
	 * length corresponding to the symbol<code>257+n</code>.
	 */
	static final int[] LENGTH_ENUM = {
		0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
		3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
	};

	/*
	 * Here, we initialize LENGTH[] from LENGTH_ENUM[].
	 */
	static {
		LENGTH = new int[29];
		LENGTH[0] = 3;
		int l = 3;
		for (int i = 1; i < 28; i ++) {
			l += 1 << LENGTH_ENUM[i - 1];
			LENGTH[i] = l;
		}
		/*
		 * The RFC 1951 specifies that the last symbol specifies
		 * a copy length of 258, not 259. I don't know why.
		 */
		LENGTH[28] = 258;
	}

	/**
	 * <code>DIST[n]</code> is the copy sequence distance corresponding
	 * to the distance symbol <code>n</code>, possibly augmented by
	 * some extra bits.
	 */
	static final int[] DIST;

	/**
	 * <code>DIST_ENUM[n]</code> contains the number of extra bits
	 * used to augment the copy sequence distance corresponding to
	 * the distance symbol <code>n</code>.
	 */
	static final int[] DIST_ENUM = {
		0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7,
		8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
	};

	/*
	 * DIST[] is initialized from DIST_ENUM[].
	 */
	static {
		DIST = new int[30];
		DIST[0] = 1;
		int d = 1;
		for (int i = 1; i < 30; i ++) {
			d += 1 << DIST_ENUM[i - 1];
			DIST[i] = d;
		}
	}

	/**
	 * This array encodes the permutation for the values encoding
	 * the RLE-compressed trees.
	 */
	static final int[] PERM_CT = {
		16, 17, 18, 0, 8, 7, 9, 6, 10, 5,
		11, 4, 12, 3, 13, 2, 14, 1, 15
	};

	/**
	 * Build the canonical Huffman codes, given the length of each
	 * code. <code>null</code> is returned if the code is not
	 * correct. The returned array is trimmed to its minimal size
	 * (trailing codes which do not occur are removed). The codes
	 * are "reversed" (first bit is least significant).
	 *
	 * @param codeLen      the code lengths
	 * @param maxCodeLen   the maximum code length
	 * @return  the codes, or <code>null</code>
	 */
	static int[] makeCanonicalHuff(int[] codeLen, int maxCodeLen)
	{
		int alphLen = codeLen.length;
		int actualAlphLen = 0;

		/*
		 * Compute the number of codes for each length
		 * (by convention, there is no code of length 0).
		 */
		int[] blCount = new int[maxCodeLen + 1];
		for (int n = 0; n < alphLen; n ++) {
			int len = codeLen[n];
			if (len < 0 || len > maxCodeLen)
				return null;
			if (len > 0) {
				actualAlphLen = n + 1;
				blCount[len] ++;
			}
		}

		/*
		 * Compute the smallest code for each code length.
		 */
		int[] nextCode = new int[maxCodeLen + 1];
		int codeVal = 0;
		for (int bits = 1; bits <= maxCodeLen; bits ++) {
			codeVal = (codeVal + blCount[bits - 1]) << 1;
			nextCode[bits] = codeVal;
		}

		/*
		 * Compute the code itself for each synbol. We also
		 * count the number of distinct symbols which may appear.
		 */
		int[] code = new int[actualAlphLen];
		for (int n = 0; n < actualAlphLen; n ++) {
			int len = codeLen[n];
			if (len != 0) {
				int w = nextCode[len];
				if (w >= (1 << len))
					return null;
				code[n] = reverse(w, len);
				nextCode[len] = w + 1;
			}
		}

		return code;
	}

	/**
	 * Bit reverse a value.
	 *
	 * @param cc   the value to reverse
	 * @param q    the value length, in bits
	 * @return  the reversed value
	 */
	private static int reverse(int cc, int q)
	{
		int v = 0;
		while (q -- > 0) {
			v <<= 1;
			if ((cc & 1) != 0)
				v ++;
			cc >>>= 1;
		}
		return v;
	}
}
