/*
    Lonestar UndirectedGraph: A graph class with undirected edges

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

    File: UndirectedGraph.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.ArrayList;

/**
 * This class implements an undirected graph. It does so by defining a new type
 * called UndirectedGraphNode which aliases the inNeighbors and the outNeighbors
 * together.
 *
 * @author milind
 * @param <NodeData>
 * The data type stored at each node
 */
public class UndirectedGraph<NodeData> extends DirectedGraph<NodeData> {

  @Override
  public Node<NodeData> createNode(NodeData n) {
    return new UndirectedGraphNode(n);
  }

  /**
   * The only difference between this class and GraphNode is that inNeighbors
   * and outNeighbors are aliased to one another.
   *
   * @author milind
   */
  protected class UndirectedGraphNode extends GraphNode {
    public UndirectedGraphNode(NodeData n) {
      data = n;
      inNeighbors = new ArrayList<GraphNode>();
      outNeighbors = inNeighbors;
    }
  }
}
