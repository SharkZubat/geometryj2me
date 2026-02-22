// $Id: Inflater.java 103 2007-08-06 15:48:07Z pornin $
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

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>An <code>Inflater</code> instance handles the transformation of
 * compressed data bytes, read from a user-specified stream, into the
 * corresponding uncompressed data bytes.</p>
 *
 * <p>The engine can be reset at any time, and work over various stream
 * instances. Deflated data is organized into successive blocks, the
 * last block being marked as such. This class is <emph>not
 * adequate</emph> for asynchronous non-blocking decompression: when a
 * read request is issued, it will wait for incoming compressed data
 * bytes until the end of the current block, as long as the provided
 * output buffer is large enough.</p>
 *
 * @version   $Revision: 103 $
 * @author    Thomas Pornin
 */

public final class Inflater {

	private InputStream in;
	private int currentByte, currentPtr;
	private int[] huffLit, huffDist;
	private byte[] rotating;
	private int rotatingPtr;
	private int decLen;
	private int copyDist, copyLen;
	private boolean needsHeader, finalBlock;
	private boolean uncompressed;
	private int uncompressedLen;

	/**
	 * Create a new engine.
	 */
	public Inflater()
	{
		rotating = new byte[32768];
	}

	/**
	 * Reset the engine, to work over the provided stream.
	 *
	 * @param in   the input stream for compressed data
	 */
	public void reset(InputStream in)
	{
		setRawStream(in);
		currentPtr = 0;
		rotatingPtr = 0;
		copyLen = 0;
		needsHeader = true;
		finalBlock = false;
		uncompressed = false;
		decLen = 0;
	}

	/**
	 * Change the input stream, but keep the running state. In
	 * particular, the currently read partial byte is kept; the
	 * new stream will be used for the next byte.
	 *
	 * @param in   the new input stream for compressed data
	 */
	public void setRawStream(InputStream in)
	{
		this.in = in;
	}

	/**
	 * <p>Process a dictionary. This is stream of bytes which will
	 * be assumed to have just been decompressed. Conceptually, the
	 * compressor also used that dictionary; this allows some
	 * backwards references to byte sequences in that data
	 * stream.</p>
	 *
	 * <p>The dictionary stream is read until its end.</p>
	 *
	 * @param dict   the dictionary stream
	 * @throws IOException  on I/O error with the dictionary stream
	 */
	public void processDictionary(InputStream dict)
		throws IOException
	{
		for (;;) {
			int rlen = dict.read(rotating,
				rotatingPtr, 32768 - rotatingPtr);
			if (rlen < 0)
				return;
			decLen += rlen;
			if (decLen > 32768)
				decLen = 32768;
			rotatingPtr = (rotatingPtr + rlen) & 32767;
		}
	}

	/**
	 * <p>Process a dictionary. This is stream of bytes which will
	 * be assumed to have just been decompressed. Conceptually, the
	 * compressor also used that dictionary; this allows some
	 * backwards references to byte sequences in that data
	 * stream.</p>
	 *
	 * @param dict   the dictionary
	 */
	public void processDictionary(byte[] dict)
	{
		processDictionary(dict, 0, dict.length);
	}

	/**
	 * <p>Process a dictionary. This is stream of bytes which will
	 * be assumed to have just been decompressed. Conceptually, the
	 * compressor also used that dictionary; this allows some
	 * backwards references to byte sequences in that data
	 * stream.</p>
	 *
	 * @param dict   the dictionary
	 * @param off    the dictionary offset
	 * @param len    the dictionary length
	 */
	public void processDictionary(byte[] dict, int off, int len)
	{
		if (len >= 32768) {
			off = len - 32768;
			len = 32768;
		}
		int end = rotatingPtr + len;
		if (end > 32768) {
			int cut = 32768 - rotatingPtr;
			System.arraycopy(dict, off,
				rotating, rotatingPtr, cut);
			System.arraycopy(dict, off + cut,
				rotating, 0, len - cut);
		} else {
			System.arraycopy(dict, off,
				rotating, rotatingPtr, len);
		}
		rotatingPtr = end & 32767;
		decLen += len;
		if (decLen > 32768)
			decLen = 32768;
	}

	/**
	 * Read a 16-bit unsigned value, little-endian, byte-aligned.
	 *
	 * @return  the value
	 * @throws IOException  on I/O error, including unexpected EOF
	 */
	private int readu2()
		throws IOException
	{
		int l = in.read();
		if (l < 0)
			throw new JGZException("unexpected EOF");
		int h = in.read();
		if (h < 0)
			throw new JGZException("unexpected EOF");
		return l + (h << 8);
	}

	/**
	 * Get the next data bit.
	 *
	 * @return  <code>true</code> if the next bit is 1
	 * @throws IOException  on I/O error, including EOF
	 */
	private boolean nextBit()
		throws IOException
	{
		if (currentPtr == 0) {
			int x = in.read();
			if (x < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			currentByte = (x >>> 1);
			currentPtr = 7;
			return (x & 1) != 0;
		}
		boolean ret = ((currentByte & 1) != 0);
		currentByte >>>= 1;
		currentPtr --;
		return ret;
	}

	/*
	 * Most of the decompression works by decoding symbols from the
	 * incoming bit stream, using a Huffman tree. A Huffman tree is
	 * a binary tree where each node is either a leaf, representing
	 * a symbol, or has two child trees: the left child is used if the
	 * next bit is a 0, the right child otherwise. The tree cannot be
	 * mono-leaf, i.e. its root node cannot be a leaf (all symbols
	 * use at least 1 bit). Except for the trivial tree used for a
	 * stream with only one symbol (the root node has two sons which
	 * are leaves and both encode that exact symbol), all leaves
	 * reference distinct symbols. Hence, if the n distinct symbols
	 * appear, then the tree contains exactly n-1 non-leaf nodes.
	 *
	 * Note: all leaves must exist. We _could_ tolerate trees with
	 * missing leaves (which are trivially non-optimal) but such
	 * trees are, according to Mark Adler (one of the authors of
	 * zlib), an error which MUST be reported as such.
	 *
	 * Our base tree encoding is an array of "int". Each entry
	 * encodes a non-leaf node: its lower 16 bits encode the path
	 * to the left child, while its upper 16 bits encode the path
	 * to the right child. If such a 16-bit value has its upper (15th)
	 * bit set, then the corresponding child is a leaf with the symbol
	 * value equal to the low 15 bits. Otherwise, the low 15 bits
	 * contain the index for the non-leaf child node. The tree root
	 * is at index 0.
	 *
	 * This representation needs one array access for each input
	 * bit. In order to speed up things, we have an alternate
	 * representation with a lookup table. It so happens that the
	 * input stream provides bytes, which means that when we want to
	 * decode the next symbol, we have up to 8 bits of lookahead. If
	 * we have q look-ahead bits (1 <= q <= 8), with collective
	 * numerical value v, then we lookup element (1<<q)+v in the
	 * table. That table contains an integer: if its 15th bit is
	 * set, then the low 15 bits encode the matched symbol value,
	 * and the bits 16 and above encode the number of bits which
	 * were actually consumed (no more than q, of course). If the
	 * value does not have its 15th bit set, then all q bits were
	 * consumed and the decoder should continue from the tree node
	 * which has the value as index. If the look-ahead bits cannot
	 * actually occur, then the value is -1.
	 *
	 * The fast lookup table uses indexes from 2 to 511 (inclusive),
	 * which allows for a tree representation compatible with the
	 * non-optimized tree: the tree root is at index 0, and the rest
	 * of the tree is at indexes 512 and above. Hence, the
	 * non-optimized decode function can be used with both tree
	 * types. Non-optimized trees are faster to encode: we will use
	 * a non-optimized tree for the tree-encoder tree, and optimized
	 * trees for the main literal and distance encoder trees.
	 */

	/**
	 * Get the next symbol, using the provided Huffman tree (optimized
	 * or not).
	 *
	 * @param huff   the tree to use
	 * @return  the next symbol
	 * @throws IOException  on error (I/O error or invalid symbol)
	 */
	private int decodeSymbol(int[] huff)
		throws IOException
	{
		int state = 0;
		for (;;) {
			int m = huff[state];
			if (nextBit())
				m = (m >>> 16);
			else
				m &= 0xFFFF;
			if (m >= 0x8000) {
				m &= 0x7FFF;
				if (m == 0x7FFF)
					throw new JGZException(
						"invalid Huffman code");
				return m;
			}
			state = m;
		}
	}

	/**
	 * Get the next symbol, using the provided optimized Huffman
	 * tree.
	 *
	 * @param huff   the tree to use
	 * @return  the next symbol
	 * @throws IOException  on error (I/O error or invalid symbol)
	 */
	private int decodeSymbolOpt(int[] huff)
		throws IOException
	{
		/*
		 * In this method, we use local variables to cache
		 * the values of currentByte and currentPtr.
		 */
		int q = currentPtr;
		int v;
		if (q == 0) {
			v = in.read();
			if (v < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			q = 8;
		} else {
			v = currentByte;
		}

		/*
		 * We first use the lookup table to process the bits
		 * we already have.
		 */
		int state = huff[(1 << q) + v];
		if ((state & 0x8000) != 0) {
			if (state < 0)
				throw new JGZException(
					"invalid Huffman code");
			int consumed = (state >>> 16);
			currentByte = v >>> consumed;
			currentPtr = q - consumed;
			return state & 0x7FFF;
		}

		/*
		 * Decoding is not finished. If we had only 6 or less
		 * bits of look-ahead, then we may use the lookup table
		 * again, with 2 or more bits from the next byte. This
		 * saves some array accesses.
		 */
		if (q <= 6) {
			int v2 = in.read();
			if (v2 < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			state = huff[256 + ((v + (v2 << q)) & 0xFF)];
			if ((state & 0x8000) != 0) {
				if (state < 0)
					throw new JGZException(
						"invalid Huffman code");
				int consumed = (state >>> 16) - q;
				currentByte = v2 >>> consumed;
				currentPtr = 8 - consumed;
				return state & 0x7FFF;
			}
			v = v2 >>> (8 - q);
		} else {
			v = in.read();
			if (v < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			q = 8;
		}

		/*
		 * The lookup table sent us to some depth in the tree;
		 * we just keep going. We hardcode the behaviour of
		 * nextBit() because it allows us to use our local
		 * cached values instead of the global currentByte and
		 * currentPtr.
		 */
		for (;;) {
			for (int k = q - 1; k >= 0; k --) {
				int m = huff[state];
				if ((v & 1) != 0)
					m = (m >>> 16);
				else
					m &= 0xFFFF;
				v >>>= 1;
				if (m >= 0x8000) {
					m &= 0x7FFF;
					if (m == 0x7FFF)
						throw new JGZException(
							"invalid Huffman code");
					currentByte = v;
					currentPtr = k;
					return m;
				}
				state = m;
			}
			v = in.read();
			if (v < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			q = 8;
		}
	}

	/**
	 * Get the next <code>num</code> stream bits, and decode them
	 * into a numerical value.
	 *
	 * @param num   the number of bits
	 * @return  the decoded integer
	 * @throws IOException  on I/O error
	 */
	private int decodeExtra(int num)
		throws IOException
	{
		/*
		 * We could use nextBit() to get the value bit by bit,
		 * but since we actually read full bytes, we can gather
		 * bits by chunks.
		 *
		 * Strangely enough, removing the test on "num" makes
		 * things slightly slower on a test machine. It is
		 * possible that this test is used by the JIT compiler
		 * to assume in the rest of the code that "num" is not
		 * zero.
		 */
		if (num == 0)
			return 0;
		int q = currentPtr;
		int v;
		if (q == 0) {
			v = in.read();
			if (v < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			q = 8;
		} else {
			v = currentByte;
		}
		int e = 0, s = 0;
		for (;;) {
			if (num <= q) {
				e += (v & ((1 << num) - 1)) << s;
				currentByte = v >>> num;
				currentPtr = q - num;
				return e;
			}
			e += v << s;
			s += q;
			num -= q;
			v = in.read();
			if (v < 0)
				throw new JGZException(
					"unexpected end-of-stream");
			q = 8;
		}
	}

	/**
	 * <code>LENGTH[n]</code> contains the sequence copy length
	 * when the symbol <code>257+n</code> has been read. The actual
	 * copy length may be augmented with a value from some extra bits.
	 */
	private static final int[] LENGTH;

	/**
	 * <code>LENGTH_ENUM[n]</code> contains the number of extra bits
	 * which shall be read, in order to augment the sequence copy
	 * length corresponding to the symbol<code>257+n</code>.
	 */
	private static final int[] LENGTH_ENUM = {
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
	private static final int[] DIST;

	/**
	 * <code>DIST_ENUM[n]</code> contains the number of extra bits
	 * used to augment the copy sequence distance corresponding to
	 * the distance symbol <code>n</code>.
	 */
	private static final int[] DIST_ENUM = {
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
	 * Decompress at most <code>len</code> bytes, which go into the
	 * rotating buffer; less than <code>len</code> bytes are decoded
	 * if the special end-of-block symbol is encountered. The caller
	 * MUST make sure that <code>len</code> is not zero and is no
	 * more than 32768. This method shall not be called unless a
	 * proper compressed block header was processed and the
	 * end-of-block has not yet been reached. The
	 * <code>needsHeader</code> flag is set when the end-of-block is
	 * reached by this method.
	 *
	 * @param len   the number of bytes to decompress
	 * @return  the number of bytes acutally decompressed
	 * @throws IOException  on I/O or format error
	 */
	private int nextBytes(int len)
		throws IOException
	{
		int origLen = len;
		while (len > 0) {
			if (decLen < 0)
				decLen = 32768;
			if (copyLen > 0) {
				int clen = len;
				if (clen > copyLen)
					clen = copyLen;
				int from = (rotatingPtr - copyDist) & 32767;
				for (int i = 0; i < clen; i ++) {
					rotating[rotatingPtr] = rotating[from];
					rotatingPtr = (rotatingPtr + 1) & 32767;
					from = (from + 1) & 32767;
				}
				len -= clen;
				copyLen -= clen;
				decLen += clen;
				continue;
			}

			int s = decodeSymbolOpt(huffLit);
			if (s == 256) {
				needsHeader = true;
				return origLen - len;
			}

			if (s < 256) {
				rotating[rotatingPtr] = (byte)s;
				rotatingPtr = (rotatingPtr + 1) & 32767;
				len --;
				decLen ++;
				continue;
			}
			s -= 257;
			copyLen = LENGTH[s];
			int copyLenExtra = LENGTH_ENUM[s];
			if (copyLenExtra > 0)
				copyLen += decodeExtra(copyLenExtra);
			s = decodeSymbolOpt(huffDist);
			if (s >= 30)
				throw new JGZException(
					"invalid distance code: " + s);
			copyDist = DIST[s];
			int copyDistExtra = DIST_ENUM[s];
			if (copyDistExtra > 0)
				copyDist += decodeExtra(copyDistExtra);
			if (copyDist > decLen) {
				throw new JGZException(
					"invalid distance (oversized)");
			}
		}
		return origLen;
	}

	/**
	 * Uncompress some data from the current block. Compressed bytes
	 * are read and processed until the current block is finished,
	 * or the output buffer for uncompressed data is full, whichever
	 * comes first. If the current block is finished, then 0 is
	 * returned.
	 *
	 * @param buf   the output buffer
	 * @param off   the output buffer offset
	 * @param len   the output buffer length (in bytes)
	 * @return  the uncompressed data length (in bytes)
	 */
	public int readBlock(byte[] buf, int off, int len)
		throws IOException
	{
		int rlen = 0;
		while (len > 0) {
			/*
			 * If we have reached the current block end,
			 * then we exit.
			 */
			if (needsHeader)
				return rlen;

			/*
			 * Current block contains uncompressed data.
			 */
			if (uncompressed) {
				if (uncompressedLen == 0) {
					needsHeader = true;
					return rlen;
				}
				int clen = uncompressedLen;
				if (clen > len)
					clen = len;
				if (clen > 32768)
					clen = 32768;
				int vlen = in.read(buf, off, clen);
				if (vlen < 0)
					throw new JGZException(
						"unexpected EOF");
				int end = rotatingPtr + vlen;
				if (end > 32768) {
					int cut = 32768 - rotatingPtr;
					System.arraycopy(buf, off,
						rotating, rotatingPtr, cut);
					System.arraycopy(buf, off + cut,
						rotating, 0, vlen - cut);
				} else {
					System.arraycopy(buf, off,
						rotating, rotatingPtr, vlen);
				}
				rotatingPtr = end & 32767;
				uncompressedLen -= vlen;
				decLen += vlen;
				if (decLen < 0)
					decLen = 32768;
				off += vlen;
				len -= vlen;
				rlen += vlen;
				continue;
			}

			/*
			 * We get here if the current block is compressed.
			 * We call nextBytes(), but not for more than
			 * 32kB worth of uncompressed data. The uncompressed
			 * data will appear in the rotating buffer. The
			 * nextBytes() method sets the needsHeader flag
			 * when appropriate.
			 */
			int clen = len;
			if (clen > 32768)
				clen = 32768;
			int vlen = nextBytes(clen);
			if (vlen > 0) {
				int begin = rotatingPtr - vlen;
				if (begin < 0) {
					System.arraycopy(rotating,
						32768 + begin,
						buf, off, -begin);
					System.arraycopy(rotating, 0,
						buf, off - begin, rotatingPtr);
				} else {
					System.arraycopy(rotating, begin,
						buf, off, vlen);
				}
				rlen += vlen;
				len -= vlen;
			}
		}
		return rlen;
	}

	/**
	 * Get the next block header. This call shall be issued only if
	 * the current block (if any) has been read completely. Returned
	 * value is <code>true</code> on success (next block header read
	 * and processed), <code>false</code> otherwise (the previous
	 * block was marked final). Format error, including unexpected
	 * end-of-stream from the underlying input stream, are reported
	 * as <code>IOException</code> or subclasses thereof (e.g.
	 * <code>JGZException</code>).
	 *
	 * @return  <code>true</code> on success, <code>false</code> on
	 *          logical end of compressed stream
	 * @throws IOException  on I/O or format error
	 */
	public boolean nextBlock()
		throws IOException
	{
		if (!needsHeader)
			throw new JGZException("current block not finished");
		if (finalBlock)
			return false;
		finalBlock = nextBit();
		int type = decodeExtra(2);

		uncompressed = false;
		switch (type) {
		case 0:
			uncompressed = true;
			if (currentByte != 0)
				throw new JGZException(
					"non-zero byte alignment padding");
			currentPtr = 0;
			int len = readu2();
			int nlen = readu2();
			if ((len ^ nlen) != 0xFFFF)
				throw new JGZException("length mismatch"
					+ " on uncompressed block");
			uncompressedLen = len;
			break;
		case 1:
			huffLit = HF_LIT;
			huffDist = HF_DIST;
			break;
		case 2:
			decodeDynamic();
			break;
		default:
			throw new JGZException(
				"invalid block header (reserved)");
		}
		needsHeader = false;
		return true;
	}

	/**
	 * Uncompress some data. Compressed bytes are read and processed
	 * until the end of the final block is reached, or the output
	 * buffer for uncompressed data is full, whichever comes first.
	 * Hence, the returned value is distinct from <code>len</code>
	 * only if the end of the final block was reached. If no
	 * uncompressed byte was read, then 0 is returned.
	 *
	 * @param buf   the output buffer
	 * @param off   the output buffer offset
	 * @param len   the output buffer length (in bytes)
	 * @return  the uncompressed data length (in bytes)
	 */
	public int readAll(byte[] buf, int off, int len)
		throws IOException
	{
		int rlen = 0;
		while (len > 0) {
			if (needsHeader) {
				if (!nextBlock())
					return rlen;
			}
			int vlen = readBlock(buf, off, len);
			off += vlen;
			len -= vlen;
			rlen += vlen;
		}
		return rlen;
	}

	/*
	 * HF_LIT[] and HF_DIST[] are the fixed Huffman trees for
	 * type-1 blocks. We initialize them statically. Note that
	 * for HF_DIST[], the actual specification is that all
	 * distance codes are 5-bit strings interpreted numerically
	 * with the big-endian convention; it so happens that this
	 * is what we get if we build the Huffman tree with all
	 * codes from 0 to 31 with the same length. Codes 30 and 31
	 * cannot actually occur; the decoder filters them out.
	 */ 
	private static final int[] HF_LIT, HF_DIST;
	static {
		try {
			int[] codeLen = new int[288];
			for (int i = 0; i < 144; i ++)
				codeLen[i] = 8;
			for (int i = 144; i < 256; i ++)
				codeLen[i] = 9;
			for (int i = 256; i < 280; i ++)
				codeLen[i] = 7;
			for (int i = 280; i < 288; i ++)
				codeLen[i] = 8;
			HF_LIT = computeHuff(9, codeLen, true);

			codeLen = new int[32];
			for (int i = 0; i < 32; i ++)
				codeLen[i] = 5;
			HF_DIST = computeHuff(5, codeLen, true);
		} catch (JGZException je) {
			throw new Error(je.getMessage());
		}
	}

	/**
	 * This is the permutation for the code values for the
	 * second Huffman encoding.
	 */
	private static final int[] HUFF2PERM = {
		16, 17, 18, 0, 8, 7, 9, 6, 10, 5,
		11, 4, 12, 3, 13, 2, 14, 1, 15
	};

	/**
	 * Decode the dynamic huffman trees; this shall be called when
	 * the two-bit block type was just read and turned out to be the
	 * type 2.
	 *
	 * @throws IOException  on I/O or format error
	 */
	private void decodeDynamic()
		throws IOException
	{
		int hlit = decodeExtra(5) + 257;
		int hdist = decodeExtra(5) + 1;
		int hclen = decodeExtra(4) + 4;
		int[] h2CodeLen = new int[19];
		for (int i = 0; i < hclen; i ++)
			h2CodeLen[HUFF2PERM[i]] = decodeExtra(3);

		int[] huff2 = computeHuff(7, h2CodeLen, false);

		int tmpLen = hlit + hdist;
		int[] tmpCodeLen = new int[tmpLen];
		int p = 0;
		int prev = -1;
		while (p < tmpLen) {
			int s = decodeSymbol(huff2);
			int repeat;
			switch (s) {
			case 16:
				if (prev < 0)
					throw new JGZException(
						"repeat code at beginning");
				repeat = 3 + decodeExtra(2);
				break;
			case 17:
				prev = 0;
				repeat = 3 + decodeExtra(3);
				break;
			case 18:
				prev = 0;
				repeat = 11 + decodeExtra(7);
				break;
			default:
				tmpCodeLen[p ++] = s;
				prev = s;
				continue;
			}
			if ((p + repeat) > tmpLen)
				throw new JGZException(
					"repeat code beyond actual length");
			while (repeat -- > 0)
				tmpCodeLen[p ++] = prev;
		}

		int[] litCodeLen = new int[286];
		System.arraycopy(tmpCodeLen, 0, litCodeLen, 0, hlit);
		int[] distCodeLen = new int[32];
		System.arraycopy(tmpCodeLen, hlit, distCodeLen, 0, hdist);
		huffLit = computeHuff(15, litCodeLen, true);
		huffDist = computeHuff(15, distCodeLen, true);
	}

	/**
	 * Compute the Huffman tree for an alphabet. Symbols have values
	 * between 0 (inclusive) and <code>codeLen.length</code> (exclusive).
	 *
	 * @param maxCodeLen   the maximum length for a code
	 * @param codeLen      the length of each symbol code
	 * @param opt          <code>true</code> to produce an optimized tree
	 * @return  the Huffman tree
	 * @throws JGZException  on error
	 */
	private static int[] computeHuff(
		int maxCodeLen, int[] codeLen, boolean opt)
		throws JGZException
	{
		int alphLen = codeLen.length;

		/*
		 * Compute number of codes for each length
		 * (blCount[n] is the number of codes of length n;
		 * by convention, blCount[0] == 0).
		 */
		int[] blCount = new int[maxCodeLen + 1];
		for (int i = 0; i < alphLen; i ++) {
			int len = codeLen[i];
			if (len < 0 || len > maxCodeLen)
				throw new JGZException(
					"invalid Huffman code length");
			if (len > 0)
				blCount[len] ++;
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
		 * Compute the code itself for each symbol. We also
		 * count the number of distinct symbols which may appear.
		 */
		int[] code = new int[alphLen];
		int syms = 0;
		for (int n = 0; n < alphLen; n ++) {
			int len = codeLen[n];
			if (len != 0) {
				int w = nextCode[len];
				code[n] = w;
				if (w >= (1 << len)) {
					throw new JGZException(
						"invalid Huffman tree");
				}
				nextCode[len] = w + 1;
				syms ++;
			}
		}

		/*
		 * If there is no symbol at all, then the code is empty.
		 * This may occur for the distance alphabet, in case
		 * a block contained only literals and was encoded with
		 * dynamic Huffman codes. Fortunately, the symbol decoder
		 * tests for invalid codes.
		 */
		if (syms == 0) {
			int retLen = opt ? 512 : 1;
			int[] ret = new int[retLen];
			for (int i = 0; i < retLen; i ++)
				ret[i] = -1;
			return ret;
		}

		/*
		 * We may now build the tree. There are "syms" leaves,
		 * hence "syms-1" non-leaf nodes, except if there is a
		 * single symbol, in which case we have a single node,
		 * with both leaves containing identical symbols.
		 */
		if (syms == 1) {
			int uc = -1;
			for (int n = 0; n < alphLen; n ++) {
				if (codeLen[n] != 0) {
					uc = n;
					break;
				}
			}
			int retLen = opt ? 512 : 1;
			int[] ret = new int[retLen];
			int w = uc | 0x8000;
			ret[0] = w | (w << 16);
			w |= 1 << 16;
			for (int i = 2; i < retLen; i ++)
				ret[i] = w;
			return ret;
		}

		/*
		 * We first fill the tree array with a special value,
		 * meaning for each node "this node is not allocated".
		 * Then, for each value, we walk the tree, building the
		 * missing nodes as we go.
		 *
		 * The number of nodes in the tree is equal to n-1 where
		 * "n" is the number of leaves. Since unallocated leaves
		 * are not tolerated, "n" must equal "syms".
		 */
		int huffLen = syms - 1;
		if (opt)
			huffLen += 511;
		int[] huff = new int[huffLen];
		for (int i = 0; i < huffLen; i ++)
			huff[i] = -1;
		int ptr = opt ? 512 : 1;
		for (int n = 0; n < alphLen; n ++) {
			int len = codeLen[n];
			if (len == 0)
				continue;
			int cc = code[n];
			int i = 0;
			int m = 1 << (len - 1);
			for (int q = 1; m > 0; m >>>= 1, q ++) {
				boolean b = (cc & m) != 0;
				int h = huff[i];
				int w = b ? (h >>> 16) : (h & 0xFFFF);
				if (w == 0xFFFF) {
					int ni = i;
					if (m == 1) {
						w = 0x8000 | n;
					} else {
						ni = ptr ++;
						w = ni;
					}
					huff[i] = b
						? ((h & 0xFFFF) | (w << 16))
						: ((h & ~0xFFFF) | w);
					i = ni;
					if (i >= huffLen)
						throw new JGZException(
						    "incomplete Huffman tree");
				} else {
					if (m == 1 || (w & 0x8000) != 0)
						throw new JGZException(
							"invalid Huffman tree");

					i = w;
				}
				if (opt) {
					if (m == 1) {
						fillDone(huff, cc, len, n);
					} else {
						fillCont(huff, cc >>> (len - q),
							q, i);
					}
				}
			}
		}

		return huff;
	}

	/**
	 * Fill some fast lookup entries in the provided tree array. This
	 * is for a partial symbol: <code>q</code> read bits, of collective
	 * numerical value <code>cc</code>, which send us to state
	 * <code>s</code>. The value <code>cc</code> is in code endianness:
	 * the first encountered stream bit is the most significant bit.
	 * The value must be reversed to compute the LUT index.
	 *
	 * @param huff   the tree table
	 * @param cc     the matched code prefix (<code>q</code> bits)
	 * @param q      the matched code prefix length (in bits)
	 * @param s      the destination tree node index
	 */
	private static void fillCont(int[] huff, int cc, int q, int s)
	{
		if (q > 8)
			return;
		cc = reverse(cc, q);
		huff[(1 << q) + cc] = s;
	}

	/**
	 * Fill some fast lookup entries in the provided tree array. This
	 * is for a complete symbol: <code>q</code> read bits, of collective
	 * numerical value <code>cc</code>, resulting in symbol of value
	 * <code>v</code>. The value <code>cc</code> is in code endianness:
	 * the first encountered stream bit is the most significant bit.
	 * The value must be reversed to compute the LUT index.
	 *
	 * @param huff   the tree table
	 * @param cc     the code
	 * @param q      the code length (in bits)
	 * @param v      the symbol value
	 */
	private static void fillDone(int[] huff, int cc, int q, int v)
	{
		if (q > 8)
			return;
		cc = reverse(cc, q);
		v |= 0x8000 | (q << 16);
		huff[(1 << q) + cc] = v;
		for (int r = q + 1; r <= 8; r ++) {
			int l = 1 << (r - q);
			for (int i = 0; i < l; i ++)
				huff[(1 << r) + cc + (i << q)] = v;
		}
	}

	/**
	 * Bit-reverse a value.
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
