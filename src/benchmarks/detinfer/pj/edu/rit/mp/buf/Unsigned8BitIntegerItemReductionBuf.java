//******************************************************************************
//
// File:    Unsigned8BitIntegerItemReductionBuf.java
// Package: benchmarks.detinfer.pj.edu.ritmp.buf
// Unit:    Class benchmarks.detinfer.pj.edu.ritmp.buf.Unsigned8BitIntegerItemReductionBuf
//
// This Java source file is copyright (C) 2007 by Alan Kaminsky. All rights
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

import benchmarks.detinfer.pj.edu.ritmp.Unsigned8BitIntegerBuf;
import benchmarks.detinfer.pj.edu.ritmp.Buf;

import benchmarks.detinfer.pj.edu.ritpj.reduction.IntegerOp;
import benchmarks.detinfer.pj.edu.ritpj.reduction.Op;

import java.nio.ByteBuffer;

/**
 * Class Unsigned8BitIntegerItemReductionBuf provides a reduction buffer for
 * class {@linkplain Unsigned8BitIntegerItemBuf}.
 *
 * @author  Alan Kaminsky
 * @version 26-Oct-2007
 */
class Unsigned8BitIntegerItemReductionBuf
	extends Unsigned8BitIntegerBuf
	{

// Hidden data members.

	Unsigned8BitIntegerItemBuf myBuf;
	IntegerOp myOp;

// Exported constructors.

	/**
	 * Construct a new unsigned 8-bit integer item reduction buffer.
	 *
	 * @param  buf  Buffer containing the item.
	 * @param  op   Binary operation.
	 *
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>op</TT> is null.
	 */
	public Unsigned8BitIntegerItemReductionBuf
		(Unsigned8BitIntegerItemBuf buf,
		 IntegerOp op)
		{
		super (1);
		if (op == null)
			{
			throw new NullPointerException
				("Unsigned8BitIntegerItemReductionBuf(): op is null");
			}
		myBuf = buf;
		myOp = op;
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
	public int get
		(int i)
		{
		return myBuf.item;
		}

	/**
	 * Store the given item in this buffer. The item at index <TT>i</TT> in this
	 * buffer is combined with the given <TT>item</TT> using the binary
	 * operation.
	 * <P>
	 * The <TT>put()</TT> method must not block the calling thread; if it does,
	 * all message I/O in MP will be blocked.
	 *
	 * @param  i     Item index in the range 0 .. <TT>length()</TT>-1.
	 * @param  item  Item to be stored at index <TT>i</TT>.
	 */
	public void put
		(int i,
		 int item)
		{
		myBuf.item = myOp.op (myBuf.item, item);
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
		throw new UnsupportedOperationException();
		}

// Hidden operations.

	/**
	 * Send as many items as possible from this buffer to the given byte
	 * buffer.
	 * <P>
	 * The <TT>sendItems()</TT> method must not block the calling thread; if it
	 * does, all message I/O in MP will be blocked.
	 *
	 * @param  i       Index of first item to send, in the range 0 ..
	 *                 <TT>length</TT>-1.
	 * @param  buffer  Byte buffer.
	 *
	 * @return  Number of items sent.
	 */
	protected int sendItems
		(int i,
		 ByteBuffer buffer)
		{
		if (buffer.remaining() >= 1)
			{
			buffer.put ((byte) myBuf.item);
			return 1;
			}
		else
			{
			return 0;
			}
		}

	/**
	 * Receive as many items as possible from the given byte buffer to this
	 * buffer. As the items are received, they are combined with the items in
	 * this buffer using the binary operation.
	 * <P>
	 * The <TT>receiveItems()</TT> method must not block the calling thread; if
	 * it does, all message I/O in MP will be blocked.
	 *
	 * @param  i       Index of first item to receive, in the range 0 ..
	 *                 <TT>length</TT>-1.
	 * @param  num     Maximum number of items to receive.
	 * @param  buffer  Byte buffer.
	 *
	 * @return  Number of items received.
	 */
	protected int receiveItems
		(int i,
		 int num,
		 ByteBuffer buffer)
		{
		if (num >= 1 && buffer.remaining() >= 1)
			{
			myBuf.item = myOp.op (myBuf.item, buffer.get() & 0xFF);
			return 1;
			}
		else
			{
			return 0;
			}
		}

	}
