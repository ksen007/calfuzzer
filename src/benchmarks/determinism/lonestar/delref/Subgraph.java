/*
    Lonestar DelaunayRefinement: Refinement of an initial, unrefined Delaunay
    mesh to eliminate triangles with angles < 30 degrees, using a variation
    of Chew's algorithm.

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

    File: Subgraph.java

    Modified: December 2, 2007 by Milind Kulkarni (initial version)
    Modified: Apr. 22, 2009 by Milind Kulkarni (version 2.0)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class Subgraph {
  private final LinkedList<Node<Element>> nodes;
  // the nodes in the graph before updating
  private final LinkedList<Node<Element>> border;  // the internal edges in the subgraph
  private final LinkedList<Edge<Element.Edge>> edges;
  // the edges that connect the subgraph to the rest of the graph

  public Subgraph() {
    nodes = new LinkedList<Node<Element>>();
    border = new LinkedList<Node<Element>>();
    edges = new LinkedList<Edge<Element.Edge>>();
  }

  public boolean existsNode(Node<Element> n) {
    return nodes.contains(n);
  }

  public boolean existsBorder(Node<Element> b) {
    return border.contains(b);
  }

  public boolean existsEdge(Edge<Element.Edge> e) {
    return edges.contains(e);
  }

  public boolean addNode(Node<Element> n) {
    return nodes.add(n);
  }

  public boolean addBorder(Node<Element> b) {
    return border.add(b);
  }

  public void addEdge(Edge<Element.Edge> e) {
    edges.add(e);
  }

  public LinkedList<Node<Element>> getNodes() {
    return nodes;
  }

  public LinkedList<Node<Element>> getBorder() {
    return border;
  }

  public LinkedList<Edge<Element.Edge>> getEdges() {
    return edges;
  }

  public void reset() {
    nodes.clear();
    border.clear();
    edges.clear();
  }

  public HashSet<Node<Element>> newBad(EdgeGraph<Element, Element.Edge> mesh) {
    HashSet<Node<Element>> ret = new HashSet<Node<Element>>();
    Iterator<Node<Element>> iter = nodes.iterator();
    while (iter.hasNext()) {
      Node<Element> node = iter.next();
      Element element = mesh.getNodeData(node);
      if (element.isBad()) {
        ret.add(node);
      }
    }
    return ret;
  }
}
