/*
    Lonestar EdgeGraph: A directed graph interface with edge information

    Author: Milind Kulkarni
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

    File: EdgeGraph.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.Collection;

/**
 * This variant of graph provides explicit Edge objects which can have arbitrary
 * data (e.g. weights) associated with them
 *
 * @author milind
 */
public interface EdgeGraph<NodeData, EdgeData> extends Graph<NodeData> {

  /**
   * Factory method to create a new Edge object with associated EdgeData which will connect src to dest
   * Note that this <b>does not</b> add the edge to the graph. This merely constructs the edge object.
   * To add the edge to the graph, call @link{addEdge}.
   *
   * @param src  The source of the edge
   * @param dest The dest of the edge
   * @param e    The data to associate with the edge
   * @return The newly created Edge object.
   */
  Edge<EdgeData> createEdge(Node<NodeData> src, Node<NodeData> dest, EdgeData e);

  /**
   * Given two nodes, src and dest, returns the edge (src, dest) connecting them, if if exists
   *
   * @param src  The source node for the edge
   * @param dest The destination node for the edge
   * @return The edge (src, dest), if it exists, null otherwise
   */
  Edge<EdgeData> getEdge(Node<NodeData> src, Node<NodeData> dest);

  /**
   * Remove an edge from the graph
   *
   * @param e The edge to remove
   * @return true if the edge was removed
   */
  boolean removeEdge(Edge<EdgeData> e);

  /**
   * Add an edge to the graph
   *
   * @param e The edge to add
   * @return true if the edge was not already in the graph
   */
  boolean addEdge(Edge<EdgeData> e);

  /**
   * Check whether an edge is in the graph
   *
   * @param e The edge to check
   * @return true if e is in the graph
   */
  boolean hasEdge(Edge<EdgeData> e);

  /**
   * Given an edge (src, dest), get src
   *
   * @param e The edge to examine
   * @return If e = (src, dest), returns src
   */
  Node<NodeData> getSource(Edge<EdgeData> e);

  /**
   * Given an edge (src, dest), get dest
   *
   * @param e The edge to examine
   * @return IF e = (src, dest), returns dest
   */
  Node<NodeData> getDest(Edge<EdgeData> e);

  /**
   * Return the edges leading away from n
   *
   * @param n The node to get edges for
   * @return The set of all edges (n, m) in the graph
   */
  Collection<? extends Edge<EdgeData>> getOutEdges(Node<NodeData> n);

  /**
   * Return the edges leading to n
   *
   * @param n The node to get edges for
   * @return The set of all edges (m, n) in the graph
   */
  Collection<? extends Edge<EdgeData>> getInEdges(Node<NodeData> n);

  /**
   * Get the data associated with an edge
   *
   * @param e The edge in question
   * @return The EdgeData object associated with e
   */
  EdgeData getEdgeData(Edge<EdgeData> e);

  /**
   * Set the data associated with an edge
   *
   * @param e The edge in question
   * @param d The data to associate with e
   * @return The data previously associated with e
   */
  EdgeData setEdgeData(Edge<EdgeData> e, EdgeData d);
}
