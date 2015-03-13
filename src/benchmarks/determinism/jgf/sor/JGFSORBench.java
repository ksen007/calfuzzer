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
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package benchmarks.determinism.jgf.sor;
import benchmarks.determinism.jgf.jgfutil.*;
import java.util.Random;

public class JGFSORBench extends SOR implements JGFSection2{

  private int size;
  private int datasizes[]={1000,1500,2000};
  private static final int JACOBI_NUM_ITER = 100;
  private static final long RANDOM_SEED = 10101010;
  public static int nthreads;

  Random R = new Random(RANDOM_SEED);

  public JGFSORBench(int nthreads){
    this.nthreads = nthreads;
  }

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){

  }

  public void JGFkernel(){

   double G[][] = RandomMatrix(datasizes[size], datasizes[size],R);

    SORrun(1.25, G, JACOBI_NUM_ITER);


  }

  public void JGFvalidate(){

    double refval[] = {0.498574406322512,1.1234778980135105,1.9954895063582696};
    double dev = Math.abs(Gtotal - refval[size]);
    if (dev > 1.0e-12 ){
      System.out.println("Validation failed");
      System.out.println("Gtotal = " + Gtotal + "  " + dev + "  " + size);
    }
  }

  public void JGFtidyup(){
   System.gc();
  }



  public void JGFrun(int size){


    JGFInstrumentor.addTimer("Section2:SOR:Kernel", "Iterations",size);

    JGFsetsize(size);
    JGFinitialise();
    JGFkernel();
    JGFvalidate();
    JGFtidyup();


    JGFInstrumentor.addOpsToTimer("Section2:SOR:Kernel", (double) (JACOBI_NUM_ITER));

    JGFInstrumentor.printTimer("Section2:SOR:Kernel");
  }

 private static double[][] RandomMatrix(int M, int N, java.util.Random R)
  {
                double A[][] = new double[M][N];

        for (int i=0; i<N; i++)
                        for (int j=0; j<N; j++)
                        {
                A[i][j] = R.nextDouble() * 1e-6;
                        }
                return A;
        }


}
