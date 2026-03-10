// $Id: GZipOutputStream.java 94 2007-08-06 08:54:24Z pornin $
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
import java.io.OutputStream;

/**
 * <p>This class represents a stream which compress data into the
 * <code>gzip</code> format, as specified in RFC 1952.</p>
 *
 * <p><code>gzip</code> streams contain meta-information in various
 * fields, some of which being optional. This class class sets these
 * fields to default, rather uninformative values: the OS field is set
 * to 255 ("unknown"), the original file name is not included, and the
 * original file modification time is set to 0. These values are legal,
 * according to RFC 1952, and almost no application uses these fields
 * anyway.</p>
 *
 * <p>The compression level can be specified, as a symbolic value
 * identical to what the <code>Deflater</code> class expects. The
 * default compression level is <code>MEDIUM</code>.</p>
 *
 * <p>Since compression is inherently buffered, the provided stream
 * needs not feature buffers.</p>
 *
 * @version   $Revision: 94 $
 * @author    Thomas Pornin
 */

public class GZipOutputStream extends OutputStream {

	private OutputStream out;
	private Deflater deflater;
	private byte[] oneByte;
	private int size, crc;

	/**
	 * Create the stream with the provided transport stream. The
	 * default compression level (<code>MEDIUM</code>) is used.
	 *
	 * @param out   the transport stream
	 * @throws IOException  on I/O error with the transport stream
	 */
	public GZipOutputStream(OutputStream out)
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
	public GZipOutputStream(OutputStream out, int level)
		throws IOException
	{
		this.out = out;
		deflater = new Deflater(level);
		deflater.setOut(out);
		oneByte = new byte[1];
		size = 0;
		crc = CRC.START;
		byte[] tmp = new byte[10];
		tmp[0] = (byte)0x1F;
		tmp[1] = (byte)0x8B;
		tmp[2] = (byte)GZipDecoder.DEFLATE;
		tmp[9] = (byte)0xFF;
		out.write(tmp);
	}

	/**
	 * Write out a 32-bit integer on the stream (little-endian).
	 *
	 * @param v   the value
	 * @throws IOException  on I/O error with the transport stream
	 */
	private void writes4(int v)
		throws IOException
	{
		out.write(v);
		out.write(v >>> 8);
		out.write(v >>> 16);
		out.write(v >>> 24);
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
	 * <code>gzip</code> file are written on the transport stream
	 * (but the transport stream itself is not flushed). Note that
	 * <code>gzip</code> streams are made of several "members" and
	 * there is no indication that a member is the last. Hence, the
	 * receiver will need another way to decide whether this member
	 * is the last.
	 *
	 * @throws IOException  on I/O error with the transport stream
	 */
	public void terminate()
		throws IOException
	{
		deflater.terminate();
		writes4(~crc);
		writes4(size);
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
		deflater.flushSync(true);
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
		deflater.process(buf, off, len);
		crc = CRC.updateCRC(crc, buf, off, len);
		size += len;
	}
}
