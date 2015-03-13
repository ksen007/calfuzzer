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

    File: SerialDelaunayrefinement.java

    Modified: December 2, 2007 by Milind Kulkarni (initial version)
    Modified: Apr. 22, 2009 by Milind Kulkarni (version 2.0)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.Stack;

public class SerialDelaunayrefinement {
  public static void main(String[] args) {
    long starttime, endtime, runtime, lasttime, mintime, run;

    System.err.println("");
    System.err.println("Lonestar benchmark suite");
    System.err.println("Copyright (C) 2007, 2008, 2009 The University of Texas at Austin");
    System.err.println("http://iss.ices.utexas.edu/lonestar/");
    System.err.println("");
    System.err.println("application: DelaunayRefinement v2.0");

    if (args.length < 1) {
      System.err.println("");
      System.err.println("arguments: input_file_name [v]");
      System.exit(-1);
    }

    boolean verify = false;

    if (args.length == 2) {
      if (args[1].equals("v")) {
        verify = true;
      }
    }

    runtime = 0;
    lasttime = Long.MAX_VALUE;
    mintime = Long.MAX_VALUE;
    run = 0;

    EdgeGraph<Element, Element.Edge> mesh = null;
    while (((run < 3) || (Math.abs(lasttime-runtime)*64 > Math.min(lasttime, runtime))) && (run < 7)) {
      mesh = new UndirectedEdgeGraph<Element, Element.Edge>();
      Stack<Node<Element>> worklist;

      Mesh.read(mesh, args[0]);

      worklist = new Stack<Node<Element>>();
      worklist.addAll(Mesh.getBad(mesh));

      Cavity cavity = new Cavity(mesh);

      if (run == 0) {
        System.err.println("");
        System.err.println("Configuration");
        System.err.println("-------------");
        System.err.println("Input: " + args[0]);
        System.err.println("Mesh size: " + mesh.getNumNodes() + " triangles");
        System.err.println("Initial bad triangles: " + worklist.size());
        System.err.println("");
      }

      System.gc();  System.gc();  System.gc();  System.gc();  System.gc();
      lasttime = runtime;
      endtime = 0;
      starttime = System.nanoTime();

      while (!worklist.isEmpty()) {
        Node<Element> bad_element = worklist.pop();
        if ((bad_element != null) && (mesh.containsNode(bad_element))) {
          cavity.initialize(bad_element);
          cavity.build();
          cavity.update();

          //remove the old data
          for (Node<Element> node : cavity.getPre().getNodes()) {
            mesh.removeNode(node);
          }

          //add new data
          for (Node<Element> node : cavity.getPost().getNodes()) {
            mesh.addNode(node);
          }
          for (Edge<Element.Edge> edge : cavity.getPost().getEdges()) {
            mesh.addEdge(edge);
          }

          worklist.addAll(cavity.getPost().newBad(mesh));
          if (mesh.containsNode(bad_element)) {
            worklist.add(bad_element);
          }
        }
      }

      endtime = System.nanoTime();
      runtime = endtime - starttime;

      if ((run == 0) || (runtime < mintime)) mintime = runtime;
      run++;
    }

    System.err.println("runtime: " + (mintime/1000000) + " ms");
    System.err.println("");

    if (verify) {
      if (Mesh.verify(mesh)) {
        int size = Mesh.getBad(mesh).size();
        if (size == 0) {
          System.out.println("OK");
        }
      }
    }
  }
}
