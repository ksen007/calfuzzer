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


package benchmarks.detinfer.jgf.sparsematmult;

import benchmarks.detinfer.jgf.jgfutil.*;
import java.util.Random;

public class JGFSparseMatmultBench extends SparseMatmult implements JGFSection2{

  public static int nthreads;

  private int size;
  private static final long RANDOM_SEED = 10101010;

  private static final int datasizes_M[] = {50000,100000,500000};
  private static final int datasizes_N[] = {50000,100000,500000};
  private static final int datasizes_nz[] = {250000,500000,2500000};
  private static final int SPARSE_NUM_ITER = 200;

  Random R = new Random(RANDOM_SEED);

  double [] x;
  double [] y;
  double [] val;
  int [] col;
  int [] row;
  int [] lowsum;
  int [] highsum;

  public JGFSparseMatmultBench(int nthreads) {
   this.nthreads = nthreads;
  }

  public void JGFsetsize(int size){
    this.size = size;

  }

  public void JGFinitialise(){

  x = RandomVector(datasizes_N[size], R);
  y = new double[datasizes_M[size]];

  val = new double[datasizes_nz[size]];
  col = new int[datasizes_nz[size]];
  row = new int[datasizes_nz[size]];

  int [] ilow = new int[nthreads];
  int [] iup = new int[nthreads];
  int [] sum = new int[nthreads+1];
  lowsum = new int[nthreads+1];
  highsum = new int[nthreads+1];
  int [] rowt = new int[datasizes_nz[size]];
  int [] colt = new int[datasizes_nz[size]];
  double [] valt = new double[datasizes_nz[size]];
  int sect;

    for (int i=0; i<datasizes_nz[size]; i++) {

        // generate random row index (0, M-1)
        row[i] = Math.abs(R.nextInt()) % datasizes_M[size];

        // generate random column index (0, N-1)
        col[i] = Math.abs(R.nextInt()) % datasizes_N[size];

        val[i] = R.nextDouble();

    }

// reorder arrays for parallel decomposition

    sect = (datasizes_M[size] + nthreads-1)/nthreads;

    for (int i=0; i<nthreads; i++) {
      ilow[i] = i*sect;
      iup[i] = ((i+1)*sect)-1;
      if(iup[i] > datasizes_M[size]) iup[i] = datasizes_M[size];
    }

    for (int i=0; i<datasizes_nz[size]; i++) {
      for (int j=0; j<nthreads; j++) {
        if((row[i] >= ilow[j]) && (row[i] <= iup[j])) {
          sum[j+1]++;
        }
      }
    }

    for (int j=0; j<nthreads; j++) {
      for (int i=0; i<=j; i++) {
         lowsum[j] = lowsum[j] + sum[j-i];
         highsum[j] = highsum[j] + sum[j-i];
      }
    }

    for (int i=0; i<datasizes_nz[size]; i++) {
      for (int j=0; j<nthreads; j++) {
        if((row[i] >= ilow[j]) && (row[i] <= iup[j])) {
          rowt[highsum[j]] = row[i];
          colt[highsum[j]] = col[i];
          valt[highsum[j]] = val[i];
          highsum[j]++;
        }
      }
    }

    for (int i=0; i<datasizes_nz[size]; i++) {
      row[i] = rowt[i];
      col[i] = colt[i];
      val[i] = valt[i];
    }

  }

  public void JGFkernel(){

    SparseMatmult.test(y, val, row, col, x, SPARSE_NUM_ITER, lowsum, highsum);

  }

  public void JGFvalidate(){

    double refval[] = {75.02484945753453,150.0130719633895,749.5245870753752};
    double dev = Math.abs(ytotal - refval[size]);
    if (dev > 1.0e-10 ){
      System.out.println("Validation failed");
      System.out.println("ytotal = " + ytotal + "  " + dev + "  " + size);
    }

  }

  public void JGFtidyup(){
   System.gc();
  }



  public void JGFrun(int size){


    JGFInstrumentor.addTimer("Section2:SparseMatmult:Kernel", "Iterations",size);

    JGFsetsize(size);
    JGFinitialise();
    JGFkernel();
    JGFvalidate();
    JGFtidyup();


    JGFInstrumentor.addOpsToTimer("Section2:SparseMatmult:Kernel", (double) (SPARSE_NUM_ITER));

    JGFInstrumentor.printTimer("Section2:SparseMatmult:Kernel");
  }

        private static double[] RandomVector(int N, java.util.Random R)
        {
                double A[] = new double[N];

                for (int i=0; i<N; i++)
                        A[i] = R.nextDouble() * 1e-6;

                return A;
        }


}
