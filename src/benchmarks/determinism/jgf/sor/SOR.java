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

package benchmarks.determinism.jgf.sor;
import benchmarks.determinism.jgf.jgfutil.*;

import static edu.berkeley.cs.detcheck.Determinism.openDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.closeDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.requireDeterministic;
import static edu.berkeley.cs.detcheck.Determinism.assertDeterministic;
import static edu.berkeley.cs.detcheck.Predicate.ApproxEquals;

public class SOR
{

  public static double Gtotal = 0.0;
  public static final int cachelinesize = 128;
  public static volatile long sync[][];

	public static final void SORrun(double omega, double G[][], int num_iterations)
	{
            openDeterministicBlock();
            requireDeterministic(omega, new ApproxEquals(1e-14));
            requireDeterministic(G, new ApproxEquals(1e-14));
            requireDeterministic(num_iterations);

		int M = G.length;
		int N = G[0].length;

		double omega_over_four = omega * 0.25;
		double one_minus_omega = 1.0 - omega;


		// update interior points
		//
		int Mm1 = M-1;
		int Nm1 = N-1;

                SORRunner thobjects[] = new SORRunner[JGFSORBench.nthreads];
                Thread th[] = new Thread[JGFSORBench.nthreads];
                sync = init_sync(JGFSORBench.nthreads);

                JGFInstrumentor.startTimer("Section2:SOR:Kernel");

                for(int i=1;i<JGFSORBench.nthreads;i++) {
                    thobjects[i] = new SORRunner(i,omega,G,num_iterations,sync);
                    th[i] = new Thread(thobjects[i]);
                    th[i].start();
                }

                    thobjects[0] = new SORRunner(0,omega,G,num_iterations,sync);
                    thobjects[0].run();


                for(int i=1;i<JGFSORBench.nthreads;i++) {
                    try {
                        th[i].join();
                    }
                    catch (InterruptedException e) {}
                }


                // assertDeterministic(G, new ApproxEquals(1e-10));
                // closeDeterministicBlock();

                JGFInstrumentor.stopTimer("Section2:SOR:Kernel");

                for (int i=1; i<Nm1; i++) {
                 for (int j=1; j<Nm1; j++) {
                  Gtotal += G[i][j];
                 }
                }

                assertDeterministic(Gtotal, new ApproxEquals(1e-10));
                closeDeterministicBlock();
	}

    private static  long[][] init_sync(int nthreads) {
        long sync[][] = new long [JGFSORBench.nthreads][cachelinesize];
        for (int i = 0; i<JGFSORBench.nthreads; i++)
            sync[i][0] = 0;
        return sync;
    }

}


class SORRunner implements Runnable {

    int id,num_iterations;
    double G[][],omega;
    double Gsum[];
    volatile long sync[][];

    public SORRunner(int id, double omega, double G[][], int num_iterations,long[][] sync) {
        this.id = id;
        this.omega=omega;
        this.G=G;
        this.num_iterations=num_iterations;
        this.sync=sync;
        this.Gsum = new double[G.length];
    }

    public void run() {

        int M = G.length;
        int N = G[0].length;

        double omega_over_four = omega * 0.25;
        double one_minus_omega = 1.0 - omega;

        // update interior points
        //
        int Mm1 = M-1;
        int Nm1 = N-1;


        int ilow, iupper, slice, tslice, ttslice;

        tslice = (Mm1) / 2;
        ttslice = (tslice + JGFSORBench.nthreads-1)/JGFSORBench.nthreads;
        slice = ttslice*2;

        ilow=id*slice+1;
        iupper = ((id+1)*slice)+1;
        if (iupper > Mm1) iupper =  Mm1+1;
        if (id == (JGFSORBench.nthreads-1)) iupper = Mm1+1;

        for (int p=0; p<2*num_iterations; p++) {
           for (int i=ilow+(p%2); i<iupper; i=i+2) {


             double [] Gi = G[i];
             double [] Gim1 = G[i-1];


             if(i == 1) {
                 double [] Gip1 = G[i+1];

                 double sum = 0;
                 for (int j=1; j<Nm1; j=j+2){
                     double t = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j-1]
                                                   + Gi[j+1]) + one_minus_omega * Gi[j];
                     Gi[j] = t;
                     sum += Gi[j];
                 }
                 Gsum[i] = sum;

             } else if (i == Mm1) {

                 double [] Gim2 = G[i-2];

                 double sum = 0;
                 for (int j=1; j<Nm1; j=j+2){
                     if((j+1) != Nm1) {
                         double t = omega_over_four * (Gim2[j+1] + Gi[j+1] + Gim1[j]
                                                       + Gim1[j+2]) + one_minus_omega * Gim1[j+1];
                         Gim1[j+1] = t;
                         sum += t;
                     }
                 }
                 Gsum[i-2] = sum;

             } else {

                 double [] Gip1 = G[i+1];
                 double [] Gim2 = G[i-2];

                 double sum1 = 0, sum2 = 0;
                 for (int j=1; j<Nm1; j=j+2){
                     double t = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j-1]
                                                   + Gi[j+1]) + one_minus_omega * Gi[j];
                     Gi[j] = t;
                     sum1 += t;
                     if((j+1) != Nm1) {
                         t = omega_over_four * (Gim2[j+1] + Gi[j+1] + Gim1[j]
                                                + Gim1[j+2]) + one_minus_omega * Gim1[j+1];
                         Gim1[j+1] = t;
                         sum2 += t;
                     }
                 }
                 Gsum[i] = sum1;
                 Gsum[i-1] = sum2;
             }

           }
           barrier();
           if (id == 0) {
               double sum = 0;
               for (int i = 0; i < Gsum.length; i++) {
                   sum += Gsum[i];
               }
               assertDeterministic(sum, new ApproxEquals(1e-10));
           }
           barrier();
        }
    }

    private void barrier() {
        // Signal this thread has done iteration
        sync[id][0]++;

        // Wait for neighbours;
        if (id > 0) {
            while (sync[id-1][0] < sync[id][0]) ;
        }
        if (id < JGFSORBench.nthreads -1) {
            while (sync[id+1][0] < sync[id][0]) ;
        }
    }
}
