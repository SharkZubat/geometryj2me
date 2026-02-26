// $Id: GZipDecoder.java 94 2007-08-06 08:54:24Z pornin $
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
 * <p>This class implements a generic decoder for <code>gzip</code>
 * stream headers. A <code>gzip</code> stream is a concatenation of
 * "members". Each member is compressed with a method (the only
 * currently defined method is number 8, for the "deflate" algorithm)
 * and is associated with some meta-information, such as the source file
 * name or last modification time.</p>
 *
 * <p>A <code>GZipDecoder</code> instance can be used to decode the
 * member headers. Note that this class does not handle decompression
 * itself: the decompressor must be invoked externally. The
 * <code>gzip</code> format (documented in RFC 1952) assumes that
 * compressed data is self-terminated; hence, the member header does not
 * contain any indication as to the compressed data length.</p>
 *
 * <p>Each member begins with a header, followed by the compressed data,
 * and then a member footer. The member footer contains the uncompressed
 * data length (truncated to 32-bit) and a 32-bit CRC checksum on the
 * uncompressed data; both values shall be used to check successful
 * decompression. The stream processing should go thus:
 * <ol>
 * <li>Decode the next member header.</li>
 * <li>Process the meta-information, if needed.</li>
 * <li>Uncompress the data; optionaly, keep a count of the uncompressed
 * data length, and a running CRC.</li>
 * <li>Decode the member footer; optionaly, check the uncompressed data
 * length and CRC value.</li>
 * <li>Get back to step 1.</li>
 * </ol></p>
 *
 * <p>Check of length and CRC are <emph>highly recommanded</emph>. There
 * is no indication, neither in the header nor in the footer, about
 * whether the member is final or not. Hence, the end of the underlying
 * stream should provide that information. Such end-of-stream may occur
 * only immediately after a member footer.</p>
 *
 * <p>The <code>get*()</code> methods provide access to data obtained by
 * decoding the member header or footer. Unless otherwise specified, the
 * data is made available when the header has been decoded; the
 * uncompressed data size and CRC are decoded from the footer, which means
 * that their value becomes available only after a call to
 * <code>closeMember()</code>.</p>
 *
 * @version   $Revision: 94 $
 * @author    Thomas Pornin
 */

public class GZipDecoder {

	private InputStream sub;
	private CRCInputStream in;

	private int compressionMethod;
	private int flags;
	private int mtime;
	private int flagsExtra;
	private int os;
	private String fnameOrig;
	private String comment;
	private int uncompressedCRC;
	private int uncompressedSize;

	/** Header flag: compressed data is probably an ASCII text. */
	public static final int FTEXT     = 0x01;

	/** Header flag: header has its own CRC checksum. */
	public static final int FHCRC     = 0x02;

	/** Header flag: there is some extra data. */
	public static final int FEXTRA    = 0x04;

	/** Header flag: there is a source file name. */
	public static final int FNAME     = 0x08;

	/** Header flag: there is a comment field. */
	public static final int FCOMMENT  = 0x10;

	/** Compression method: "deflate" algorithm (RFC 1951). */
	public static final int DEFLATE   = 8;

	/**
	 * Build the decoder over the provided stream for compressed
	 * data.
	 *
	 * @param sub   the compressed data stream
	 */
	public GZipDecoder(InputStream sub)
	{
		this.sub = sub;
		in = new CRCInputStream(sub);
	}

	/**
	 * Read one data byte; on error or EOF, an exception is thrown.
	 *
	 * @return  the byte value (between 0 and 255)
	 * @throws IOException  on I/O error or EOF
	 */
	private int read1()
		throws IOException
	{
		int x = in.read();
		if (x < 0)
			throw new JGZException("unexpected end-of-file");
		return x;
	}

	/**
	 * Read a 16-bit unsigned value (little-endian).
	 *
	 * @return  the value
	 * @throws IOException  on I/O error or EOF
	 */
	private int readu2()
		throws IOException
	{
		int l = read1();
		int h = read1();
		return l + (h << 8);
	}

	/**
	 * Read a 32-bit value (signed)(little-endian).
	 *
	 * @return  the value
	 * @throws IOException  on I/O error or EOF
	 */
	private int reads4()
		throws IOException
	{
		int l = readu2();
		int h = readu2();
		return l + (h << 16);
	}

	/**
	 * Read a zero-terminated latin-1 string.
	 *
	 * @return  the string
	 * @throws IOException  on I/O error or EOF
	 */
	private String readLatin1String()
		throws IOException
	{
		StringBuffer sb = new StringBuffer();
		for (;;) {
			int v = read1();
			if (v == 0)
				return sb.toString();
			sb.append((char)v);
		}
	}

	/**
	 * Decode the next member header. If a member header was found
	 * and correctly decoded, then the underlying data stream is
	 * returned; the compressed data shall be read and processed
	 * from that stream. Otherwise, if the end-of-stream was reached
	 * when trying to get the first header byte, then <code>null</code>
	 * is returned. Otherwise (at least one byte was read but no
	 * valid header was found), an <code>IOException</code> is thrown.
	 *
	 * @return  the compressed data stream, or <code>null</code>
	 * @throws IOException  on I/O or format error
	 */
	public InputStream nextMember()
		throws IOException
	{
		in.resetCRC();
		int id1 = in.read();
		if (id1 < 0)
			return null;
		if (id1 != 0x1F || read1() != 0x8B)
			throw new JGZException("wrong magic number");
		compressionMethod = read1();
		flags = read1();
		mtime = reads4();
		flagsExtra = read1();
		os = read1();
		if ((flags & FEXTRA) != 0) {
			int len = readu2();
			while (len > 0)
				read1();
		}
		if ((flags & FNAME) != 0)
			fnameOrig = readLatin1String();
		if ((flags & FCOMMENT) != 0)
			comment = readLatin1String();
		if ((flags & FHCRC) != 0) {
			int exp = in.getCRC() & 0xFFFF;
			int res = readu2();
			if (exp != res)
				throw new JGZException("bad header CRC");
		}
		return sub;
	}

	/**
	 * Decode the member footer. This method shall be called after
	 * the processing of the compressed data.
	 *
	 * @throws IOException  on I/O or format error
	 */
	public void closeMember()
		throws IOException
	{
		uncompressedCRC = reads4();
		uncompressedSize = reads4();
	}

	/**
	 * Get the compression method.
	 *
	 * @return  the compression method
	 */
	public int getCompressionMethod()
	{
		return compressionMethod;
	}

	/**
	 * Get the header flags (8-bit value).
	 *
	 * @return  the header flags
	 */
	public int getFlags()
	{
		return flags;
	}

	/**
	 * Get the source file last modification time (32-bit "unix" time,
	 * as a number of seconds since the Epoch).
	 *
	 * @return  the source file last modification time
	 */
	public int getMTime()
	{
		return mtime;
	}

	/**
	 * Get the extra flags (8 bits).
	 *
	 * @return  the extra flags
	 */
	public int getFlagsExtra()
	{
		return flagsExtra;
	}

	/**
	 * Get the stream producer operating system (1-byte identifier).
	 *
	 * @return  the stream producer operating system
	 */
	public int getOS()
	{
		return os;
	}

	/**
	 * Get the data source file name (optional). If present, the
	 * source file name may contain any character except the null
	 * character; in particular, it may contains slashes,
	 * backslashes, dots and various control characters. All file
	 * name individual characters have values between 1 and 255
	 * (inclusive).
	 *
	 * @return  the source file name, or <code>null</code>
	 */
	public String getOriginalFileName()
	{
		return fnameOrig;
	}

	/**
	 * Get the member comment (optional). The comment is meant for
	 * human consumption. Note that nothing in this class checks
	 * that the comment contains only printable characters. All
	 * comment individual characters have values between 1 and 255
	 * (inclusive).
	 *
	 * @return  the member comment, or <code>null</code>
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * Get the CRC over uncompressed data (32-bit value, from the
	 * member footer).
	 *
	 * @return  the uncompressed data checksum
	 */
	public int getUncompressedCRC()
	{
		return uncompressedCRC;
	}

	/**
	 * Get the uncompressed data size for this member (truncated to
	 * 32 bits). This value is obtained from the member footer.
	 *
	 * @return  the uncompressed data size
	 */
	public int getUncompressedSize()
	{
		return uncompressedSize;
	}

	/**
	 * Get the underlying stream which this decoder uses. This
	 * method can always be called.
	 *
	 * @return  the underlying stream for compressed data
	 */
	public InputStream getSubStream()
	{
		return sub;
	}
}
