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

    File: Mesh.java

    Modified: December 2, 2007 by Milind Kulkarni (initial version)
    Modified: Apr. 22, 2009 by Milind Kulkarni (version 2.0)
*/

package benchmarks.determinism.lonestar.delref;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;

public class Mesh {
  private static final HashMap<Element.Edge, Node<Element>> edge_map =
      new HashMap<Element.Edge, Node<Element>>();

  public static Node<Element> addElement(EdgeGraph<Element, Element.Edge> mesh,
      Element element) {
    Node<Element> node = mesh.createNode(element);
    mesh.addNode(node);
    for (int i = 0; i < element.numEdges(); i++) {
      Element.Edge edge = element.getEdge(i);
      if (!edge_map.containsKey(edge)) {
        edge_map.put(edge, node);
      } else {
        Edge<Element.Edge> new_edge = mesh.createEdge(node, edge_map.get(edge), edge);
        mesh.addEdge(new_edge);
        edge_map.remove(edge);
      }
    }
    return node;
  }

  @SuppressWarnings("unchecked")
  public static HashSet<Node<Element>> getBad(EdgeGraph<Element, Element.Edge> mesh) {
    HashSet<Node<Element>> ret = new HashSet<Node<Element>>();
    for (Node node : mesh) {
      Element element = mesh.getNodeData(node);
      if (element.isBad()) {
        ret.add(node);
      }
    }
    return ret;
  }

  // .poly contains the perimeter of the mesh; edges basically, which is why it contains pairs of nodes
  public static void read(EdgeGraph<Element, Element.Edge> mesh, String filename) {
    Scanner instrm = null;
    try {
      String nodeinfile = filename + ".node";
      instrm = new Scanner(new File(nodeinfile));
    } catch (FileNotFoundException e) {
      System.out.println(e);
      System.exit(-1);
    }
    @SuppressWarnings("unused") int a, b, c, ntups;
    ntups = instrm.nextInt();
    a = instrm.nextInt();
    b = instrm.nextInt();
    c = instrm.nextInt();

    Tuple[] tuples = new Tuple[ntups];
    for (int i = 0; i < ntups; i++) {
      int index = instrm.nextInt();
      double x = instrm.nextDouble();
      double y = instrm.nextDouble();
      @SuppressWarnings("unused") double z = instrm.nextDouble();
      tuples[index] = new Tuple(x, y, 0);
    }

    try {
      String eleinfile = filename + ".ele";
      instrm = new Scanner(new File(eleinfile));
    } catch (FileNotFoundException e) {
      System.out.println(e);
      System.exit(-1);
    }
    int nels = instrm.nextInt();
    a = instrm.nextInt();
    b = instrm.nextInt();
    Element[] elements = new Element[nels];
    for (int i = 0; i < nels; i++) {
      int index = instrm.nextInt();
      int n1 = instrm.nextInt();
      int n2 = instrm.nextInt();
      int n3 = instrm.nextInt();
      elements[index] = new Element(tuples[n1], tuples[n2], tuples[n3]);
      addElement(mesh, elements[index]);
    }
    try {
      String polyinfile = filename + ".poly";
      instrm = new Scanner(new File(polyinfile));
    } catch (FileNotFoundException e) {
      System.out.println(e);
      System.exit(-1);
    }
    @SuppressWarnings("unused") int temp = instrm.nextInt();
    a = instrm.nextInt();
    b = instrm.nextInt();
    c = instrm.nextInt();
    int nsegs = instrm.nextInt();
    a = instrm.nextInt();
    Element[] segments = new Element[nsegs];
    for (int i = 0; i < nsegs; i++) {
      int index = instrm.nextInt();
      int n1 = instrm.nextInt();
      int n2 = instrm.nextInt();
      temp = instrm.nextInt();
      segments[index] = new Element(tuples[n1], tuples[n2]);
      addElement(mesh, segments[index]);
    }
  }

  public static boolean verify(EdgeGraph<Element, Element.Edge> mesh) {
    // ensure consistency of elements
    for (Node<Element> node : mesh) {
      Element element = mesh.getNodeData(node);
      if (element.getDim() == 2) {
        if (mesh.getOutNeighbors(node).size() != 1) {
          System.out.println(
              "-> Segment " + element + " has " + mesh.getOutNeighbors(node).size() +
                  " relation(s)");
          return false;
        }
      } else if (element.getDim() == 3) {
        if (mesh.getOutNeighbors(node).size() != 3) {
          System.out.println(
              "-> Triangle " + element + " has " + mesh.getOutNeighbors(node).size() +
                  " relation(s)");
          return false;
        }
      } else {
        System.out.println("-> Figures with " + element.getDim() + " edges");
        return false;
      }
    }

    // ensure reachability
    Node<Element> start = mesh.getRandom();
    Stack<Node<Element>> remaining = new Stack<Node<Element>>();
    HashSet<Node<Element>> found = new HashSet<Node<Element>>();
    remaining.push(start);
    while (!remaining.isEmpty()) {
      Node<Element> node = remaining.pop();
      if (!found.contains(node)) {
        found.add(node);
        for (Node<Element> neighbor : mesh.getOutNeighbors(node)) {
          remaining.push(neighbor);
        }
      }
    }
    if (found.size() != mesh.getNumNodes()) {
      System.out.println("Not all elements are reachable");
      return false;
    }

    return true;
  }
}
