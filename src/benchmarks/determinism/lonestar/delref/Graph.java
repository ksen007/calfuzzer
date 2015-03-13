/*
    Lonestar Graph: A general graph interface

    Authors: Milind Kulkarni and Martin Burtscher
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

    File: Graph.java
    Modified: Apr. 20, 2009 by Milind Kulkarni and Martin Burtscher (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.Collection;
import java.util.Iterator;

public interface Graph<NodeData> extends Iterable<Node<NodeData>> {

  /* Node manipulation */

  /**
   * Factory method to create a new Node holding data n
   *
   * @param n The object the new Node will wrap
   */
  Node<NodeData> createNode(NodeData n);

  /**
   * Add a node to the graph
   *
   * @param n The node to add to the graph
   * @return true if the Node was not already in the graph
   */
  boolean addNode(Node<NodeData> n);

  /**
   * Remove a node from the graph. Removes the node, as well as all edges
   * connected to the node
   *
   * @param n The node to remove from the graph
   * @return true if the Node was removed from the graph
   */
  boolean removeNode(Node<NodeData> n);

  /**
   * Check if a Node is in the graph
   *
   * @param n The node to check for
   * @return true if the Node is in the graph
   */
  boolean containsNode(Node<NodeData> n);

  /**
   * Get a random Node from the graph
   *
   * @return A randomly selected Node from the graph
   */
  Node<NodeData> getRandom();

  /**
   * Add an "edge" between src and dest
   *
   * @param src  The source node of the edge
   * @param dest The target node of the edge
   * @return true if the edge was not already in the graph
   */
  boolean addNeighbor(Node<NodeData> src, Node<NodeData> dest);

  /**
   * Remove the "edge" (src, dest) from the graph
   *
   * @param src  The source node of the edge
   * @param dest The target node of the edge
   * @return true if the edge was removed from the graph
   */
  boolean removeNeighbor(Node<NodeData> src, Node<NodeData> dest);

  /**
   * Check if the "edge" (src, dest) is in the graph
   *
   * @param src  The source node of the edge
   * @param dest The target node of the edge
   * @return true if the edge is in the graph
   */
  boolean hasNeighbor(Node<NodeData> src, Node<NodeData> dest);

  /**
   * Returns the "in-neighbors" of a node. In other words, return all Nodes n
   * such that edge (n, src) exists in the graph.
   *
   * @param src The node that is the target of the incoming edges
   * @return The collection of nodes that have edges pointing to src
   */
  Collection<? extends Node<NodeData>> getInNeighbors(Node<NodeData> src);

  /**
   * Returns the "out-neighbors" of a node. In other words, return all Nodes n
   * such that edge (src, n) exists in the graph.
   *
   * @param src The node that is the source of the outgoing edges
   * @return The collection of nodes that are pointed to by src
   */
  Collection<? extends Node<NodeData>> getOutNeighbors(Node<NodeData> src);

  /**
   * Determine how many nodes are in the Graph
   *
   * @return the number of nodes in the graph
   */
  int getNumNodes();

  /**
   * Get the NodeData object associated with a node
   *
   * @param n The node whose node data object is being retrieved
   * @return The NodeData object associated with n
   */
  NodeData getNodeData(Node<NodeData> n);

  /**
   * Set the NodeData object for a node
   *
   * @param n The node whose NodeData object to set
   * @param d The data to set
   * @return The old NodeData object associated with the node
   */
  NodeData setNodeData(Node<NodeData> n, NodeData d);

  @Override
  Iterator<Node<NodeData>> iterator();
}
