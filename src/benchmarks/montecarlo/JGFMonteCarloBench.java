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


package benchmarks.montecarlo;

import benchmarks.jgfutil.*;

public class JGFMonteCarloBench extends CallAppDemo implements JGFSection3 {

  public static int nthreads;

  public JGFMonteCarloBench(int nthreads) {
        this.nthreads=nthreads;
  }

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){

      initialise();

  }

  public void JGFapplication(){ 

    JGFInstrumentor.startTimer("Section3:MonteCarlo:Run");  

    runiters();

    JGFInstrumentor.stopTimer("Section3:MonteCarlo:Run");  
    
    presults();
  } 


  public void JGFvalidate(){
   double refval[] = {-0.0333976656762814,-0.03215796752868655};
   double dev = Math.abs(AppDemo.JGFavgExpectedReturnRateMC - refval[size]);
   if (dev > 1.0e-12 ){
     System.out.println("Validation failed");
     System.out.println(" expectedReturnRate= " + AppDemo.JGFavgExpectedReturnRateMC + "  " + dev + "  " + size);
    }
  }

  public void JGFtidyup(){    

    System.gc();
  }


  public void JGFrun(int size){

    JGFInstrumentor.addTimer("Section3:MonteCarlo:Total", "Solutions",size);
    JGFInstrumentor.addTimer("Section3:MonteCarlo:Run", "Samples",size);

    JGFsetsize(size); 

    JGFInstrumentor.startTimer("Section3:MonteCarlo:Total");

    JGFinitialise(); 
    JGFapplication(); 
    JGFvalidate(); 
    JGFtidyup(); 

    JGFInstrumentor.stopTimer("Section3:MonteCarlo:Total");

    JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Run", (double) input[1] );
    JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Total", 1);

    JGFInstrumentor.printTimer("Section3:MonteCarlo:Run"); 
    JGFInstrumentor.printTimer("Section3:MonteCarlo:Total"); 
  }


}
 
