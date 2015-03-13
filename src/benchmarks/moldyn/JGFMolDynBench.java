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


package benchmarks.moldyn;

import benchmarks.jgfutil.*;

public class JGFMolDynBench extends md implements JGFSection3 {

  public static int nthreads;

  public JGFMolDynBench(int nthreads) {
        this.nthreads=nthreads;
  }


//   int size;

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){

      initialise();

  }

  public void JGFapplication(){ 

    runiters();

  } 


  public void JGFvalidate(){
    double refval[] = {1731.4306625334357,7397.392307839352};
    double dev = Math.abs(ek[0] - refval[size]);
    if (dev > 1.0e-10 ){
      System.out.println("Validation failed");
      System.out.println("Kinetic Energy = " + ek[0] + "  " + dev + "  " + size);
    }
  }

  public void JGFtidyup(){    

//    one = null;
    System.gc();
  }


  public void JGFrun(int size){

    JGFInstrumentor.addTimer("Section3:MolDyn:Total", "Solutions",size);
    JGFInstrumentor.addTimer("Section3:MolDyn:Run", "Interactions",size);

    JGFsetsize(size); 

    JGFInstrumentor.startTimer("Section3:MolDyn:Total");

    JGFinitialise(); 
    JGFapplication(); 
    JGFvalidate(); 
    JGFtidyup(); 

    JGFInstrumentor.stopTimer("Section3:MolDyn:Total");

    JGFInstrumentor.addOpsToTimer("Section3:MolDyn:Run", (double) interactions);
    JGFInstrumentor.addOpsToTimer("Section3:MolDyn:Total", 1);

    JGFInstrumentor.printTimer("Section3:MolDyn:Run"); 
    JGFInstrumentor.printTimer("Section3:MolDyn:Total"); 
  }


}
 
