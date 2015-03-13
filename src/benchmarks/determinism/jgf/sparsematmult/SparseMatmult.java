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
*      adapted from SciMark 2.0, author Roldan Pozo (pozo@cam.nist.gov)   *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/

package benchmarks.determinism.jgf.sparsematmult;

import benchmarks.determinism.jgf.jgfutil.*;

import static edu.berkeley.cs.detcheck.Determinism.openDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.closeDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.requireDeterministic;
import static edu.berkeley.cs.detcheck.Determinism.assertDeterministic;
import static edu.berkeley.cs.detcheck.Predicate.ApproxEquals;
import static edu.berkeley.cs.detcheck.Predicate.ArrayEquals;

public class SparseMatmult
{

  public static double ytotal = 0.0;
  public  static double yt[];

	/* 10 iterations used to make kernel have roughly
		same granulairty as other Scimark kernels. */

	public static void test( double y[], double val[], int row[],
				int col[], double x[], int NUM_ITERATIONS, int lowsum[], int highsum[])
	{
        int nz = val.length;
        yt=y;

        SparseRunner thobjects[] = new SparseRunner[JGFSparseMatmultBench.nthreads];
        Thread th[] = new Thread[JGFSparseMatmultBench.nthreads];

        openDeterministicBlock();
        requireDeterministic(val, new ApproxEquals(1e-15));
        requireDeterministic(row, new ArrayEquals());
        requireDeterministic(col, new ArrayEquals());
        requireDeterministic(x, new ApproxEquals(1e-15));

        JGFInstrumentor.startTimer("Section2:SparseMatmult:Kernel");

        for(int i=1;i<JGFSparseMatmultBench.nthreads;i++) {
          thobjects[i] = new SparseRunner(i,val,row,col,x,NUM_ITERATIONS,nz,lowsum,highsum);
          th[i] = new Thread(thobjects[i]);
          th[i].start();
        }

        thobjects[0] = new SparseRunner(0,val,row,col,x,NUM_ITERATIONS,nz,lowsum,highsum);
        thobjects[0].run();

        for(int i=1;i<JGFSparseMatmultBench.nthreads;i++) {
          try {
           th[i].join();
          } catch (InterruptedException e) {}
        }


        JGFInstrumentor.stopTimer("Section2:SparseMatmult:Kernel");

        assertDeterministic(y, new ApproxEquals(1e-10));
        closeDeterministicBlock();

          for (int i=0; i<nz; i++) {
            ytotal += yt[ row[i] ];
          }

	}
}


class SparseRunner implements Runnable {

    int id,nz,row[],col[],NUM_ITERATIONS;
    double val[],x[];
    int lowsum[];
    int highsum[];

   public SparseRunner(int id, double val[], int row[],int col[], double x[], int NUM_ITERATIONS,int nz, int lowsum[], int highsum[]) {
        this.id = id;
        this.x=x;
        this.val=val;
        this.col=col;
        this.row=row;
        this.nz=nz;
        this.NUM_ITERATIONS=NUM_ITERATIONS;
        this.lowsum = lowsum;
        this.highsum = highsum;
    }

   public void run() {

         for (int reps=0; reps<NUM_ITERATIONS; reps++) {
           for (int i=lowsum[id]; i<highsum[id]; i++) {
             SparseMatmult.yt[ row[i] ] += x[ col[i] ] * val[i];
           }
         }

   }


}
