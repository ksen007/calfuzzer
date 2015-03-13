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

    File: Cavity.java

    Modified: December 2, 2007 by Milind Kulkarni (initial version)
    Modified: Apr. 22, 2009 by Milind Kulkarni (version 2.0)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Cavity {
  private Tuple center;
  private Node<Element> centerNode;
  private Element centerElement;
  private int dim;
  private final Queue<Node<Element>> frontier;
  private final Subgraph pre;  // the cavity itself
  private final Subgraph post;  // what the new elements should look like
  private final EdgeGraph<Element, Element.Edge> graph;
  private final HashSet<Edge<Element.Edge>> connections;
  // the edge-relations that connect the boundary to the cavity

  public Cavity(EdgeGraph<Element, Element.Edge> mesh) {
    center = null;
    frontier = new LinkedList<Node<Element>>();
    pre = new Subgraph();
    post = new Subgraph();
    graph = mesh;
    connections = new HashSet<Edge<Element.Edge>>();
  }

  public Subgraph getPre() {
    return pre;
  }

  public Subgraph getPost() {
    return post;
  }

  public void triggerAbort() {
  }

  public void triggerBorderConflict() {
  }

  public void initialize(Node<Element> node) {
    pre.reset();
    post.reset();
    connections.clear();
    frontier.clear();
    centerNode = node;
    centerElement = graph.getNodeData(centerNode);
    while (graph.containsNode(centerNode) && centerElement.isObtuse()) {
      Edge<Element.Edge> oppositeEdge = getOpposite(centerNode);
      if (graph.getSource(oppositeEdge) == centerNode) {
        centerNode = graph.getDest(oppositeEdge);
      } else {
        centerNode = graph.getSource(oppositeEdge);
      }
      centerElement = graph.getNodeData(centerNode);
      if (centerNode == null) {
        System.exit(-1);
      }
    }
    center = centerElement.center();
    dim = centerElement.getDim();
    pre.addNode(centerNode);
    frontier.add(centerNode);
  }

  // find the edge that is opposite the obtuse angle of the element
  private Edge<Element.Edge> getOpposite(Node<Element> node) {
    Element element = graph.getNodeData(node);

    Collection<? extends Node<Element>> neighbors = graph.getOutNeighbors(node);
    if (neighbors.size() != 3) {
      throw new Error(String.format("neighbors %d", neighbors.size()));
    }

    for (Node<Element> neighbor : neighbors) {
      Edge<Element.Edge> edge = graph.getEdge(node, neighbor);
      Element.Edge edge_data = graph.getEdgeData(edge);
      if (element.getObtuse().notEquals(edge_data.getPoint(0)) &&
          element.getObtuse().notEquals(edge_data.getPoint(1))) {
        return edge;
      }
    }
    throw new Error("edge");
  }

  public boolean isMember(Node<Element> node) {
    Element element = graph.getNodeData(node);
    return element.inCircle(center);
  }

  public void build() {
    while (frontier.size() != 0) {
      Node<Element> curr = frontier.poll();
      Collection<? extends Node<Element>> neighbors = graph.getOutNeighbors(curr);
      for (Node<Element> next : neighbors) {
        Element nextElement = graph.getNodeData(next);
        Edge<Element.Edge> edge = graph.getEdge(curr, next);
        if (isMember(next)) {  // isMember says next is part of the cavity
          if ((nextElement.getDim() == 2) &&
              (dim != 2)) {  // is segment, and we are encroaching
            initialize(next);
            build();
            return;
          } else {
            if (!pre.existsNode(next)) {
              pre.addNode(next);
              pre.addEdge(edge);
              frontier.add(next);
            }
          }
        } else {  // not a member
          if (!connections.contains(edge)) {
            connections.add(edge);
            pre.addBorder(next);
          }
        }
      }
    }
  }

  public void update() {
    if (centerElement.getDim() == 2) {  // we built around a segment
      Element ele1 = new Element(center, centerElement.getPoint(0));
      Node<Element> node1 = graph.createNode(ele1);
      post.addNode(node1);
      Element ele2 = new Element(center, centerElement.getPoint(1));
      Node<Element> node2 = graph.createNode(ele2);
      post.addNode(node2);
    }

    //for (Edge conn : new HashSet<Edge>(connections)) {
    for (Edge<Element.Edge> conn : connections) {
      Element.Edge edge = graph.getEdgeData(conn);
      Element new_element = new Element(center, edge.getPoint(0), edge.getPoint(1));

      Node<Element> ne_node = graph.createNode(new_element);

      Node<Element> ne_connection;
      if (pre.existsNode(graph.getDest(conn))) {
        ne_connection = graph.getSource(conn);
      } else {
        ne_connection = graph.getDest(conn);
      }

      Element.Edge new_edge =
          new_element.getRelatedEdge(graph.getNodeData(ne_connection));
      post.addEdge(graph.createEdge(ne_node, ne_connection, new_edge));

      Collection<Node<Element>> postnodes =
          (Collection<Node<Element>>) post.getNodes().clone();
      for (Node<Element> node : postnodes) {
        Element element = graph.getNodeData(node);
        if (element.isRelated(new_element)) {
          Element.Edge ele_edge = new_element.getRelatedEdge(element);
          post.addEdge(graph.createEdge(ne_node, node, ele_edge));
        }
      }

      post.addNode(ne_node);
    }
  }
}
