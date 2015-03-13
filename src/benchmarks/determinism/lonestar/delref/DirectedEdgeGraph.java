/*
    Lonestar DirectedEdgeGraph: A directed graph class with edge information

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

    File: DirectedEdgeGraph.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DirectedEdgeGraph<NodeData, EdgeData> implements EdgeGraph<NodeData, EdgeData> {

  Set<EdgeGraphNode> nodes;

  public DirectedEdgeGraph() {
    nodes = Collections.synchronizedSet(new HashSet<EdgeGraphNode>());
  }

  @Override
  public boolean addEdge(Edge<EdgeData> e) {
    GraphEdge ge = (GraphEdge) e;
    EdgeGraphNode src = ge.getSrc();
    EdgeGraphNode dest = ge.getDest();

    // if src.addOutEdge(dest, ge), then return dest.addInEdge(src, ge) [true],
    // else return false
    return (src.addOutEdge(dest, ge)) ? dest.addInEdge(src, ge) : false;
  }

  @Override
  public Edge<EdgeData> createEdge(Node<NodeData> src, Node<NodeData> dest, EdgeData e) {
    return new GraphEdge((EdgeGraphNode) src, (EdgeGraphNode) dest, e);
  }

  @Override
  public Node<NodeData> getDest(Edge<EdgeData> e) {
    return ((GraphEdge) e).getDest();
  }

  @Override
  public Edge<EdgeData> getEdge(Node<NodeData> src, Node<NodeData> dest) {
    return ((EdgeGraphNode) src).getOutEdge((EdgeGraphNode) dest);
  }

  @Override
  public Collection<? extends Edge<EdgeData>> getInEdges(Node<NodeData> n) {
    return ((EdgeGraphNode) n).getInEdges();
  }

  @Override
  public Collection<? extends Edge<EdgeData>> getOutEdges(Node<NodeData> n) {
    return ((EdgeGraphNode) n).getOutEdges();
  }

  @Override
  public Node<NodeData> getSource(Edge<EdgeData> e) {
    return ((GraphEdge) e).src;
  }

  @Override
  public boolean hasEdge(Edge<EdgeData> e) {
    GraphEdge ge = (GraphEdge) e;
    return (ge.getSrc().hasOutNeighbor(ge.getDest()));
  }

  @Override
  public boolean removeEdge(Edge<EdgeData> e) {
    GraphEdge ge = (GraphEdge) e;
    EdgeGraphNode src = ge.getSrc();
    EdgeGraphNode dest = ge.getDest();
    return (src.removeOutEdge(dest)) ? dest.removeInEdge(src) : false;
  }

  @Override
  public boolean addNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    throw new UnsupportedOperationException(
        "addNeighbor not supported in EdgeGraphs. Use createEdge/addEdge instead");
  }

  @Override
  public Node<NodeData> createNode(NodeData n) {
    return new EdgeGraphNode(n);
  }

  @Override
  public Collection<? extends Node<NodeData>> getInNeighbors(Node<NodeData> src) {
    return ((EdgeGraphNode) src).getInNeighbors();
  }

  @Override
  public Collection<? extends Node<NodeData>> getOutNeighbors(Node<NodeData> src) {
    return ((EdgeGraphNode) src).getOutNeighbors();
  }

  @Override
  public boolean removeNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    EdgeGraphNode gsrc = (EdgeGraphNode) src;
    EdgeGraphNode gdest = (EdgeGraphNode) dest;

    //if gsrc.removeOutEdge(gdest), then return gdest.removeInEdge(gsrc) [true], else return false.
    return (gsrc.removeOutEdge(gdest)) ? gdest.removeInEdge(gsrc) : false;
  }

  @Override
  public EdgeData getEdgeData(Edge<EdgeData> e) {
    return ((GraphEdge) e).d;
  }

  @Override
  public EdgeData setEdgeData(Edge<EdgeData> e, EdgeData d) {
    GraphEdge ge = (GraphEdge) e;
    EdgeData retval = ge.d;
    ge.d = d;
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

  @Override
  public boolean addNode(Node<NodeData> n) {
    return nodes.add((EdgeGraphNode) n);
  }

  @Override
  public boolean containsNode(Node<NodeData> n) {
    return nodes.contains(n);
  }

  @Override
  public NodeData getNodeData(Node<NodeData> n) {
    EdgeGraphNode egn = (EdgeGraphNode) n;
    return egn.data;
  }

  @Override
  public int getNumNodes() {
    return nodes.size();
  }

  @Override
  public Node<NodeData> getRandom() {
    return Sets.getAny(nodes);
  }

  @Override
  public boolean hasNeighbor(Node<NodeData> src, Node<NodeData> dest) {
    EdgeGraphNode esrc = (EdgeGraphNode) src;
    EdgeGraphNode edest = (EdgeGraphNode) dest;
    return (esrc.hasOutNeighbor(edest));
  }

  @Override
  public boolean removeNode(Node<NodeData> n) {
    removeConnectingEdges((EdgeGraphNode) n);
    return nodes.remove(n);
  }

  protected void removeConnectingEdges(EdgeGraphNode n) {
    Collection<EdgeGraphNode> outNeighbors = n.getOutNeighborsCopy();
    for (EdgeGraphNode g : outNeighbors) {
      removeNeighbor(n, g);
    }

    Collection<EdgeGraphNode> inNeighbors = n.getInNeighborsCopy();
    for (EdgeGraphNode g : inNeighbors) {
      removeNeighbor(g, n);
    }
  }

  @Override
  public NodeData setNodeData(Node<NodeData> n, NodeData d) {
    EdgeGraphNode egn = (EdgeGraphNode) n;
    NodeData retval = egn.data;
    egn.data = d;
    return retval;
  }

  protected class EdgeGraphNode implements Node<NodeData> {
    protected Map<EdgeGraphNode, GraphEdge> inEdges;
    protected Map<EdgeGraphNode, GraphEdge> outEdges;

    protected NodeData data;

    EdgeGraphNode() {
    }

    EdgeGraphNode(NodeData d) {
      inEdges = new HashMap<EdgeGraphNode, GraphEdge>();
      outEdges = new HashMap<EdgeGraphNode, GraphEdge>();
      data = d;
    }

    final protected boolean hasInNeighbor(EdgeGraphNode n) {
      return inEdges.containsKey(n);
    }

    /**
     * Record the edge (n, this)
     *
     * @param n The source of the edge
     * @param e The edge object
     * @return True if the edge was successfully added, false otherwise
     */
    protected boolean addInEdge(EdgeGraphNode n, GraphEdge e) {
      if (hasInNeighbor(n)) {
        return false;
      }
      inEdges.put(n, e);
      return true;
    }

    /**
     * Remove the edge (if any) (n, this)
     *
     * @param n The source of the edge
     * @return true if an edge was removed, false otherwise
     */
    protected boolean removeInEdge(EdgeGraphNode n) {
      if (!hasInNeighbor(n)) {
        return false;
      }
      inEdges.remove(n);
      return true;
    }

    /**
     * Return the edge (n, this)
     *
     * @param n The source of the edge
     * @return The edge object connecting n to this (if one exists)
     */
    protected GraphEdge getInEdge(EdgeGraphNode n) {
      return inEdges.get(n);
    }

    /**
     * Get all the edges (n, this)
     *
     * @return A collection of all edges (n, this)
     */
    protected Collection<GraphEdge> getInEdges() {
      return inEdges.values();
    }

    final protected Collection<EdgeGraphNode> getInNeighbors() {
      return inEdges.keySet();
    }

    final protected Collection<EdgeGraphNode> getInNeighborsCopy() {
      return new ArrayList<EdgeGraphNode>(inEdges.keySet());
    }

    final protected boolean hasOutNeighbor(EdgeGraphNode n) {
      return outEdges.containsKey(n);
    }

    /**
     * Record the edge (this, n)
     *
     * @param n The target of the edge
     * @param e The edge object
     * @return true if the edge was successfully added, false otherwise
     */
    protected boolean addOutEdge(EdgeGraphNode n, GraphEdge e) {
      if (hasOutNeighbor(n)) {
        return false;
      }
      outEdges.put(n, e);
      return true;
    }

    /**
     * Remove the edge (if any) (this, n)
     *
     * @param n The target of the edge
     * @return true if an edge was removed, false otherwise
     */
    protected boolean removeOutEdge(EdgeGraphNode n) {
      if (!hasOutNeighbor(n)) {
        return false;
      }
      outEdges.remove(n);
      return true;
    }

    /**
     * Return the edge (this, n)
     *
     * @param n The target of the edge
     * @return The edge object connecting this to n (if one exists)
     */
    protected GraphEdge getOutEdge(EdgeGraphNode n) {
      return outEdges.get(n);
    }

    /**
     * Get all the edges (this, n)
     *
     * @return A collection of all edges (this, n)
     */
    protected Collection<GraphEdge> getOutEdges() {
      return outEdges.values();
    }

    final protected Collection<EdgeGraphNode> getOutNeighbors() {
      return outEdges.keySet();
    }

    final protected Collection<EdgeGraphNode> getOutNeighborsCopy() {
      return new ArrayList<EdgeGraphNode>(outEdges.keySet());
    }

    @Override
    public NodeData getData() {
      return getNodeData(this);
    }

    @Override
    public NodeData setData(NodeData n) {
      return setNodeData(this, n);
    }
  }

  protected class GraphEdge implements Edge<EdgeData> {

    protected EdgeGraphNode src;
    protected EdgeGraphNode dest;

    protected EdgeData d;

    public GraphEdge(EdgeData d) {
      this.d = d;
    }

    public GraphEdge(EdgeGraphNode src, EdgeGraphNode dest, EdgeData d) {
      this(d);
      this.src = src;
      this.dest = dest;
    }

    final protected EdgeGraphNode getOpposite(EdgeGraphNode n) {
      return (n == src) ? dest : src;
    }

    final protected EdgeGraphNode getSrc() {
      return src;
    }

    final protected EdgeGraphNode getDest() {
      return dest;
    }

    @Override
    public EdgeData getData() {
      return getEdgeData(this);
    }

    @Override
    public EdgeData setData(EdgeData e) {
      return setEdgeData(this, e);
    }
  }
}
