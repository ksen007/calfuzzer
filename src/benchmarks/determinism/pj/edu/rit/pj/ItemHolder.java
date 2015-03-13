//******************************************************************************
//
// File:    ItemHolder.java
// Package: benchmarks.determinism.pj.edu.ritpj
// Unit:    Class benchmarks.determinism.pj.edu.ritpj.ItemHolder
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

package benchmarks.determinism.pj.edu.ritpj;

/**
 * Class ItemHolder provides an object that holds one item to be processed
 * by a {@linkplain ParallelIteration} along with associated information.
 *
 * @param  <T>  Data type of the items iterated over.
 *
 * @author  Alan Kaminsky
 * @version 04-Jun-2007
 */
class ItemHolder<T>
	{

// Exported data members.

	/**
	 * The item itself (may be null).
	 */
	public T myItem;

	/**
	 * The item's sequence number in the iteration. Sequence numbers start
	 * at 0 and increase by 1 for each item.
	 */
	public int mySequenceNumber;

	}
