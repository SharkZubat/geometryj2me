// $Id: JGZException.java 94 2007-08-06 08:54:24Z pornin $
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

/**
 * <p>A <code>JGZException</code> is thrown when uncompressing some
 * data bytes which turn out to violate the expected format in any
 * way.</p>
 *
 * @version   $Revision: 94 $
 * @author    Thomas Pornin
 */

public class JGZException extends IOException {

	/**
	 * Build a new exception with no message.
	 */
	public JGZException()
	{
		super();
	}

	/**
	 * Build a new exception with the provided explanatory message.
	 *
	 * @param message   the message
	 */
	public JGZException(String message)
	{
		super(message);
	}
}
