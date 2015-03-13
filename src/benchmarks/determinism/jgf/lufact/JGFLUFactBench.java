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

package benchmarks.determinism.jgf.lufact;

import benchmarks.determinism.jgf.jgfutil.*;

import static edu.berkeley.cs.detcheck.Determinism.openDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.closeDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.requireDeterministic;
import static edu.berkeley.cs.detcheck.Determinism.assertDeterministic;
import static edu.berkeley.cs.detcheck.Predicate.ApproxEquals;
import static edu.berkeley.cs.detcheck.Predicate.ArrayEquals;

public class JGFLUFactBench extends Linpack implements JGFSection2{

  public  static  int nthreads;
  private int size;
  private int datasizes[] = {500,1000,2000};
  public static final int cachelinesize = 128;

    public JGFLUFactBench(int nthreads) {
        this.nthreads=nthreads;
    }

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){

    n = datasizes[size];
    ldaa = n;
    lda = ldaa + 1;

    a = new double[ldaa][lda];
    b = new double [ldaa];
    x = new double [ldaa];
    ipvt = new int [ldaa];


    long nl = (long) n;   //avoid integer overflow
    ops = (2.0*(nl*nl*nl))/3.0 + 2.0*(nl*nl);

    norma = matgen(a,lda,n,b);

  }

  public void JGFkernel(){

    Runnable thobjects[] = new Runnable [nthreads];
    Thread th[] = new Thread [nthreads];
    Barrier br= new TournamentBarrier(nthreads);

    openDeterministicBlock();
    requireDeterministic(n);
    requireDeterministic(lda);
    requireDeterministic(a, new ApproxEquals(1e-15));
    requireDeterministic(b, new ApproxEquals(1e-15));

    JGFInstrumentor.startTimer("Section2:LUFact:Kernel");

    //Start Threads

    for(int i=1;i<nthreads;i++) {
       thobjects[i] = new LinpackRunner(i,a,lda,n,ipvt,br);
       th[i] = new Thread(thobjects[i]);
       th[i].start();
    }

       thobjects[0] = new LinpackRunner(0,a,lda,n,ipvt,br);
       thobjects[0].run();

    for(int i=1;i<nthreads;i++) {
      try {
          th[i].join();
      }
      catch (InterruptedException e) {}
    }

    assertDeterministic(a, new ApproxEquals(1e-10));
    assertDeterministic(ipvt, new ArrayEquals());

    dgesl(a,lda,n,ipvt,b,0);

    assertDeterministic(x, new ApproxEquals(1e-10));
    closeDeterministicBlock();

    JGFInstrumentor.stopTimer("Section2:LUFact:Kernel");

  }

  public void JGFvalidate(){

    int i;
    double eps,residn;
    final double ref[] = {6.0, 12.0, 20.0};

    for (i = 0; i < n; i++) {
      x[i] = b[i];
    }
    norma = matgen(a,lda,n,b);
    for (i = 0; i < n; i++) {
      b[i] = -b[i];
    }

    dmxpy(n,b,n,lda,x,a);
    resid = 0.0;
    normx = 0.0;
    for (i = 0; i < n; i++) {
      resid = (resid > abs(b[i])) ? resid : abs(b[i]);
      normx = (normx > abs(x[i])) ? normx : abs(x[i]);
    }

    eps =  epslon((double)1.0);
    residn = resid/( n*norma*normx*eps );

    if (residn > ref[size]) {
	System.out.println("Validation failed");
	System.out.println("Computed Norm Res = " + residn);
        System.out.println("Reference Norm Res = " + ref[size]);
    }

  }

    public void JGFtidyup(){
    // Make sure large arrays are gc'd.
    a = null;
    b = null;
    x = null;
    ipvt = null;
    System.gc();
  }


  public void JGFrun(int size){


    JGFInstrumentor.addTimer("Section2:LUFact:Kernel", "Mflops",size);


    JGFsetsize(size);
    JGFinitialise();
    JGFkernel();
    JGFvalidate();
    JGFtidyup();


    JGFInstrumentor.addOpsToTimer("Section2:LUFact:Kernel", ops/1.0e06);
    JGFInstrumentor.printTimer("Section2:LUFact:Kernel");
  }

}
