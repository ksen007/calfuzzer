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


public class Surface implements java.io.Serializable{
	public Vec	color;
	public double	kd;
	public double	ks;
	public double	shine;
	public double	kt;
	public double	ior;

	public Surface() {
		color = new Vec(1, 0, 0);
		kd = 1.0;
		ks = 0.0;
		shine = 0.0;
		kt = 0.0;
		ior = 1.0;
	}

	public String toString() {
		return "Surface { color=" + color + "}";
	}
}


