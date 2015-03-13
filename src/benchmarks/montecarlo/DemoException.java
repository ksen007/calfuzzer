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
*      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/



package benchmarks.montecarlo;

/**
  * Error exception, for use by the Applications Demonstrator code.
  * With optional field for selecting whether to print debug information
  * via a stack trace.
  *
  * @author H W Yau
  * @version $Revision: 1.4 $ $Date: 1999/02/16 18:51:14 $
  */
public class DemoException extends java.lang.Exception {
  /**
    * Flag for selecting whether to print the stack-trace dump.
    */
  public static boolean DEBUG=true;

  /**
    * Default constructor.
    */
  public DemoException() {
    super();
    if( DEBUG ) {
      printStackTrace();
    }
  }
  /**
    * Default constructor for reporting an error message.
    */
  public DemoException(String s) {
    super(s);
    if( DEBUG ) {
      printStackTrace();
    }
  }
  /**
    * Default constructor for reporting an error code.
    */
  public DemoException(int ierr) {
    super(String.valueOf(ierr));
    if( DEBUG ) {
      printStackTrace();
    }
  }
}
