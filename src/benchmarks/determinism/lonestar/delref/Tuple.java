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

    File: Tuple.java

    Modified: December 2, 2007 by Milind Kulkarni (initial version)
    Modified: Apr. 22, 2009 by Milind Kulkarni (version 2.0)
*/

package benchmarks.determinism.lonestar.delref;

public class Tuple {
  private final double[] coords;
  private final int hashvalue;

  public Tuple(double a, double b, double c) {
    coords = new double[3];
    coords[0] = a;
    coords[1] = b;
    coords[2] = c;

    // see Effective Java, item 8, page 33
    int tmphashvalue = 17;
    long tmp = Double.doubleToLongBits(coords[0]);
    tmphashvalue = 37 * tmphashvalue + (int) (tmp ^ (tmp >>> 32));
    tmp = Double.doubleToLongBits(coords[1]);
    tmphashvalue = 37 * tmphashvalue + (int) (tmp ^ (tmp >>> 32));
    tmp = Double.doubleToLongBits(coords[2]);
    tmphashvalue = 37 * tmphashvalue + (int) (tmp ^ (tmp >>> 32));
    hashvalue = tmphashvalue;
  }

  public Tuple(Tuple rhs) {
    coords = new double[3];
    coords[0] = rhs.coords[0];
    coords[1] = rhs.coords[1];
    coords[2] = rhs.coords[2];
    hashvalue = rhs.hashvalue;
  }

  public Tuple() {
    coords = new double[3];
    hashvalue = 1;
  }

  public double[] getCoords() {
    return coords;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Tuple)) {
      return false;
    }
    Tuple t = (Tuple) obj;
    double rhs_coords[] = t.getCoords();
    return (coords[0] == rhs_coords[0]) && (coords[1] == rhs_coords[1]) &&
        (coords[2] == rhs_coords[2]);
  }

  public int hashCode() {
    return hashvalue;
  }

  public boolean notEquals(Tuple rhs) {
    return !equals(rhs);
  }

  public boolean lessThan(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    if (coords[0] < rhs_coords[0]) {
      return true;
    }
    if (coords[0] > rhs_coords[0]) {
      return false;
    }
    if (coords[1] < rhs_coords[1]) {
      return true;
    }
    if (coords[1] > rhs_coords[1]) {
      return false;
    }
    if (coords[2] < rhs_coords[2]) {
      return true;
    }
    return false;
  }

  public boolean greaterThan(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    if (coords[0] > rhs_coords[0]) {
      return true;
    }
    if (coords[0] < rhs_coords[0]) {
      return false;
    }
    if (coords[1] > rhs_coords[1]) {
      return true;
    }
    if (coords[1] < rhs_coords[1]) {
      return false;
    }
    if (coords[2] > rhs_coords[2]) {
      return true;
    }
    return false;
  }

  public Tuple add(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    return new Tuple(coords[0] + rhs_coords[0], coords[1] + rhs_coords[1],
        coords[2] + rhs_coords[2]);
  }

  public Tuple subtract(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    return new Tuple(coords[0] - rhs_coords[0], coords[1] - rhs_coords[1],
        coords[2] - rhs_coords[2]);
  }

  // dot product
  public double dotp(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    return coords[0] * rhs_coords[0] + coords[1] * rhs_coords[1] +
        coords[2] * rhs_coords[2];
  }

  // scalar product
  public Tuple scale(double s) {
    return new Tuple(s * coords[0], s * coords[1], s * coords[2]);
  }

  public int cmp(Tuple x) {
    if (equals(x)) {
      return 0;
    }
    if (greaterThan(x)) {
      return 1;
    }
    return -1;
  }

  // distance between current tuple and rhs
  public double distance(Tuple rhs) {
    return Math.sqrt(distance_squared(rhs));
  }

  public double distance_squared(Tuple rhs) {
    double rhs_coords[] = rhs.getCoords();
    double x = coords[0] - rhs_coords[0];
    double y = coords[1] - rhs_coords[1];
    double z = coords[2] - rhs_coords[2];
    return x * x + y * y + z * z;
  }

  // angle between a, the current tuple, and c
  public double angle(Tuple a, Tuple c) {
    Tuple va = a.subtract(this);
    Tuple vc = c.subtract(this);
    double d = va.dotp(vc) / Math.sqrt(distance_squared(a) * distance_squared(c));
    return (180 / Math.PI) * Math.acos(d);
  }

  public String toString() {
    return new String("(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ")");
  }

  public static int cmp(Tuple a, Tuple b) {
    return a.cmp(b);
  }

  public static double distance(Tuple a, Tuple b) {
    return a.distance(b);
  }

  public static double angle(Tuple a, Tuple b, Tuple c) {
    return b.angle(a, c);
  }

  // the [] operation
  public double get(int i) {
    return coords[i];
  }
}
