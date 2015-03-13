/*
    Lonestar IndexedGraph: A directed graph interface for trees and DAGs

    Author: Martin Burtscher
    Center for Grid and Distributed Computing
    The University of Texas at Austin

    Copyright (C) 2007, 2008, 2009 The University of Texas at Austin

    Licensed under the Eclipse Public License, Version 1.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.eclipse.org/legal/epl-v10.html

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    File: IndexedGraph.java
    Modified: Apr. 20, 2009 by Martin Burtscher (initial version)
*/

package benchmarks.determinism.lonestar.delref;

/**
 * This interface represents a graph that allows programmers to refer to a
 * node's edges by a particular index
 *
 * @author milind
 * @param <NodeData>
 * The type of object stored at each node
 */
public interface IndexedGraph<NodeData> extends Graph<NodeData> {

  /**
   * Set a particular neighbor of a given node
   *
   * @param src   The node whose neighbor to set
   * @param index The particular neighbor to set
   * @param dest  The new neighbor
   */
  void setNeighbor(Node<NodeData> src, int index, Node<NodeData> dest);

  /**
   * Get a particular neighbor of a given node
   *
   * @param src   The node whose neighbor to get
   * @param index The particular neighbor to get
   * @return The neighbor at index
   */
  Node<NodeData> getNeighbor(Node<NodeData> src, int index);

  /**
   * Remove a particular neighbor of a given node. Note that this is equivalent
   * to calling setNeighbor(src, index, null)
   *
   * @param src   The node whose neighbor to remove
   * @param index The neighbor to remove
   */
  void removeNeighbor(Node<NodeData> src, int index);
}
