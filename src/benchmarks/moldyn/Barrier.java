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

// General barrier implementation superclass.



public abstract class Barrier {
    public Barrier(int n) {
        numThreads = n;
    }
        
    // DoBarrier() should cause each thread to wait until all threads
    // reach the barrier. The implementation can vary - see
    // definitions of TournamentBarrier for example. 
    public abstract void DoBarrier(int myid);

    // Number of threads to block for.
    public volatile int numThreads;
}
