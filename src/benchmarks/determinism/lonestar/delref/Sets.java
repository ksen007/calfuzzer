/*
    Lonestar Sets: A Set utility class for the graph classes

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

    File: Sets.java
    Modified: Apr. 22, 2009 by Milind Kulkarni (initial version)
*/

package benchmarks.determinism.lonestar.delref;

import java.util.Collection;
import java.util.NoSuchElementException;

public final class Sets {
  /**
   * Utility method to obtain an arbitrary element from a collection
   *
   * @param <E> The type contained in the collection
   * @param c   The collection to retrieve an element from
   * @return An arbitrary element from c, if one exists
   * @throws NoSuchElementException Thrown if the collection is empty
   */
  public static <E> E getAny(Collection<E> c) throws NoSuchElementException {
    return c.iterator().next();
  }
}

