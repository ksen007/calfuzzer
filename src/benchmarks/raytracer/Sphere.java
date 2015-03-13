/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         *
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                 Original version of this code by                        *
*            Florian Doyon (Florian.Doyon@sophia.inria.fr)                *
*              and  Wilfried Klauser (wklauser@acm.org)                   *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package benchmarks.raytracer;


public class Sphere extends Primitive implements java.io.Serializable {
  Vec      c;
  double   r, r2;
  Vec      v,b; // temporary vecs used to minimize the memory load

  public Sphere(Vec center, double radius) {
    c = center;
    r = radius;
    r2 = r*r;
    v=new Vec();
    b=new Vec();
  }
  
  public Isect intersect(Ray ry) {
    double b, disc, t;
    Isect ip;
    v.sub2(c, ry.P);
    b = Vec.dot(v, ry.D);
    disc = b*b - Vec.dot(v, v) + r2;
    if (disc < 0.0) {
      return null;
    }
    disc = Math.sqrt(disc);
    t = (b - disc < 1e-6) ? b + disc : b - disc;
    if (t < 1e-6) {
      return null;
    }
    ip = new Isect();
    ip.t = t;
    ip.enter = Vec.dot(v, v) > r2 + 1e-6 ? 1 : 0;
    ip.prim = this;
    ip.surf = surf;
    return ip;
  }

  public Vec normal(Vec p) {
    Vec r;
    r = Vec.sub(p, c);
    r.normalize();
    return r;
  }

  public String toString() {
    return "Sphere {" + c.toString() + "," + r + "}";
  }
	
  public Vec getCenter() {
    return c;
  }
  public void setCenter(Vec c) {
    this.c = c;
  }
}

