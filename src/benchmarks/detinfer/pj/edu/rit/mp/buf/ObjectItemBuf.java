//******************************************************************************
//
// File:    ObjectItemBuf.java
// Package: benchmarks.detinfer.pj.edu.ritmp.buf
// Unit:    Class benchmarks.detinfer.pj.edu.ritmp.buf.ObjectItemBuf
//
// This Java source file is copyright (C) 2008 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package benchmarks.detinfer.pj.edu.ritmp.buf;

import benchmarks.detinfer.pj.edu.ritmp.Buf;
import benchmarks.detinfer.pj.edu.ritmp.ObjectBuf;

import benchmarks.detinfer.pj.edu.ritpj.reduction.ObjectOp;
import benchmarks.detinfer.pj.edu.ritpj.reduction.Op;

import java.nio.ByteBuffer;

/**
 * Class ObjectItemBuf provides a buffer for a single object item sent or
 * received using the Message Protocol (MP). While an instance of class
 * ObjectItemBuf may be constructed directly, normally you will use a factory
 * method in class {@linkplain benchmarks.detinfer.pj.edu.ritmp.ObjectBuf ObjectBuf}. See that
 * class for further information.
 *
 * @param  <T>  Data type of the objects in the buffer.
 *
 * @author  Alan Kaminsky
 * @version 12-Feb-2008
 */
public class ObjectItemBuf<T>
	extends ObjectBuf<T>
	{

// Exported data members.

	/**
	 * Object item to be sent or received.
	 */
	public T item;

// Exported constructors.

	/**
	 * Construct a new object item buffer.
	 */
	public ObjectItemBuf()
		{
		super (1);
		}

	/**
	 * Construct a new object item buffer with the given initial value.
	 *
	 * @param  item  Initial value of the {@link #item} field.
	 */
	public ObjectItemBuf
		(T item)
		{
		super (1);
		this.item = item;
		}

// Exported operations.

	/**
	 * Obtain the given item from this buffer.
	 * <P>
	 * The <TT>get()</TT> method must not block the calling thread; if it does,
	 * all message I/O in MP will be blocked.
	 *
	 * @param  i  Item index in the range 0 .. <TT>length()</TT>-1.
	 *
	 * @return  Item at index <TT>i</TT>.
	 */
	public T get
		(int i)
		{
		return this.item;
		}

	/**
	 * Store the given item in this buffer.
	 * <P>
	 * The <TT>put()</TT> method must not block the calling thread; if it does,
	 * all message I/O in MP will be blocked.
	 *
	 * @param  i     Item index in the range 0 .. <TT>length()</TT>-1.
	 * @param  item  Item to be stored at index <TT>i</TT>.
	 */
	public void put
		(int i,
		 T item)
		{
		this.item = item;
		mySerializedItems = null;
		}

	/**
	 * Create a buffer for performing parallel reduction using the given binary
	 * operation. The results of the reduction are placed into this buffer.
	 *
	 * @param  op  Binary operation.
	 *
	 * @exception  ClassCastException
	 *     (unchecked exception) Thrown if this buffer's element data type and
	 *     the given binary operation's argument data type are not the same.
	 */
	public Buf getReductionBuf
		(Op op)
		{
		return new ObjectItemReductionBuf (this, (ObjectOp) op);
		}

	}
