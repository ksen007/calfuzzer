/*
    Lonestar DirectedGraph: A directed graph class

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

    File: DirectedGraph.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DirectedGraph<NodeData> implements Graph<NodeData> {

  protected Set<GraphNode> nodes;

  public DirectedGraph() {
    nodes = Collections.synchronizedSet(new HashSet<GraphNode>());
  }

  @Override
  public boolean addNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    GraphNode src_c = (GraphNode) src;
    GraphNode dest_c = (GraphNode) dest;

    // This code is equivalent to:
    // if src_c.addOutNeighbor(dest_c), then return dest_c.addInNeighbor(src_c)
    // [true], else return false
    return (src_c.addOutNeighbor(dest_c)) ? dest_c.addInNeighbor(src_c) : false;
  }

  @Override
  public boolean addNode(Node<NodeData> n) {
    return nodes.add((GraphNode) n);
  }

  @Override
  public boolean containsNode(Node<NodeData> n) {
    return nodes.contains(n);
  }

  @Override
  public Node<NodeData> createNode(NodeData n) {
    return new GraphNode(n);
  }

  @Override
  public Collection<? extends Node<NodeData>> getInNeighbors(Node<NodeData> src) {
    GraphNode src_c = (GraphNode) src;
    return Collections.unmodifiableCollection(src_c.getInNeighbors());
  }

  @Override
  public int getNumNodes() {
    return nodes.size();
  }

  @Override
  public Collection<? extends Node<NodeData>> getOutNeighbors(Node<NodeData> src) {
    GraphNode src_c = (GraphNode) src;
    return Collections.unmodifiableCollection(src_c.getOutNeighbors());
  }

  @Override
  public Node<NodeData> getRandom() {
    return Sets.getAny(nodes);
  }

  @Override
  public boolean hasNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    GraphNode src_c = (GraphNode) src;
    GraphNode dest_c = (GraphNode) dest;
    return src_c.hasOutNeighbor(dest_c);
  }

  @Override
  public boolean removeNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    GraphNode src_c = (GraphNode) src;
    GraphNode dest_c = (GraphNode) dest;

    // This code is equivalent to:
    // if src_c.removeOutNeighbor(dest_c), then return
    // dest_c.removeInNeighbor(src_c) [true], else return false;
    return (src_c.removeOutNeighbor(dest_c)) ? dest_c.removeInNeighbor(src_c) : false;
  }

  @Override
  public boolean removeNode(Node<NodeData> n) {
    // first, go through and remove all the edges connecting n to other nodes
    removeConnectingEdges((GraphNode) n);
    return nodes.remove(n);
  }

  /**
   * Remove all the edges connecting n to the rest of the graph
   *
   * @param n The node whose edges to remove
   */
  protected void removeConnectingEdges(GraphNode n) {
    Collection<GraphNode> outNeighbors = n.getOutNeighborsCopy();
    for (GraphNode g : outNeighbors) {
      removeNeighbor(n, g);
    }

    Collection<GraphNode> inNeighbors = n.getInNeighborsCopy();
    for (GraphNode g : inNeighbors) {
      removeNeighbor(g, n);
    }
  }

  @Override
  public NodeData getNodeData(Node<NodeData> n) {
    return ((GraphNode) n).data;
  }

  @Override
  public NodeData setNodeData(Node<NodeData> n, NodeData d) {
    GraphNode gn = (GraphNode) n;
    NodeData retval = gn.data;
    gn.data = d;
    return retval;
  }

  // Because Java doesn't consider Iterator<? extends Node> to be a valid
  // subtype of Iterator<Node> even though it only involves covariant
  // overriding (none of the methods in the Iterator interface have arguments).
  // Go figure.
  @SuppressWarnings("unchecked")
  @Override
  public Iterator iterator() {
    return nodes.iterator();
  }

  protected class GraphNode implements Node<NodeData> {

    protected NodeData data;
    protected List<GraphNode> inNeighbors;
    protected List<GraphNode> outNeighbors;

    protected GraphNode() {
    }

    public GraphNode(NodeData n) {
      data = n;
      inNeighbors = new ArrayList<GraphNode>();
      outNeighbors = new ArrayList<GraphNode>();
    }

    @Override
    public NodeData getData() {
      return getNodeData(this);
    }

    @Override
    public NodeData setData(NodeData n) {
      return setNodeData(this, n);
    }

    final public boolean addInNeighbor(GraphNode n) {
      if (inNeighbors.contains(n)) {
        return false;
      }
      inNeighbors.add(n);
      return true;
    }

    final public boolean removeInNeighbor(GraphNode n) {
      return inNeighbors.remove(n);
    }

    final public boolean hasInNeighbor(GraphNode n) {
      return inNeighbors.contains(n);
    }

    final public Collection<GraphNode> getInNeighbors() {
      return inNeighbors;
    }

    final public Collection<GraphNode> getInNeighborsCopy() {
      return new ArrayList<GraphNode>(inNeighbors);
    }

    final public boolean addOutNeighbor(GraphNode n) {
      if (outNeighbors.contains(n)) {
        return false;
      }
      outNeighbors.add(n);
      return true;
    }

    final public boolean removeOutNeighbor(GraphNode n) {
      return outNeighbors.remove(n);
    }

    final public boolean hasOutNeighbor(GraphNode n) {
      return outNeighbors.contains(n);
    }

    final public Collection<GraphNode> getOutNeighbors() {
      return outNeighbors;
    }

    final public Collection<GraphNode> getOutNeighborsCopy() {
      return new ArrayList<GraphNode>(outNeighbors);
    }
  }
}

