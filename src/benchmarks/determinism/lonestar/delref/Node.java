/*
    Lonestar Node: A node interface for use with the graph classes

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

    File: Node.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

public interface Node<NodeData> {
  /**
   * Get the data associated with this Node
   *
   * @return the data associated with this Node
   */
  NodeData getData();

  /**
   * Set the data wrapped by this Node
   *
   * @param n The data to wrap
   * @return The old value that used to be wrapped
   */
  NodeData setData(NodeData n);
}
