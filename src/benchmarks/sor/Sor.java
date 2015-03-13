package benchmarks.sor;
/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id$
 * @author Florian Schneider
 */

import benchmarks.EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

import java.util.Date;

public class Sor {

    public final static int N = 5;
    public final static int M = 5;
    public static int iterations = 100;


    public static float[][] black_=new float[M+2][N+1];
    public static float[][] red_=new float[M+2][N+1];

    public static int nprocs = 1;
    public static CyclicBarrier barrier;
    static Thread[] t;

    public static void main(String[] args) {

	boolean nop = false;

	try {
	    if (args[0].equals("--nop")) nop = true;
	    else {
		nprocs = Integer.parseInt(args[1]);
		iterations = Integer.parseInt(args[0]);
	    }
	} catch (Exception e) {
	    System.out.println("usage: java Sor <iterations> <number of threads>");
	    System.out.println("    or java Sor --nop");
	    System.exit(-1);
	}
	
	t = new Thread[nprocs];
	barrier = new CyclicBarrier(nprocs);

	// initialize arrays
	int first_row = 1;
	int last_row  = M;

	for (int i = first_row; i <= last_row; i++) {
	    /*
	     * Initialize the top edge.
	     */
	    if (i == 1)
		for (int j = 0; j <= N; j++)
		    red_[0][j] = black_[0][j] = (float) 1.0;
	    /*
	     * Initialize the left and right edges.
	     */
	    if ((i & 1) != 0) {
		red_[i][0] = (float) 1.0;
		black_[i][N] = (float) 1.0;
	    }
	    else {
		black_[i][0] = (float) 1.0;
		red_[i][N] = (float) 1.0;
	    }
	    /*
	     * Initialize the bottom edge.
	     */
	    if (i == M)
		for (int j = 0; j <= N; j++)
		    red_[i+1][j] = black_[i+1][j] = (float) 1.0;
	}


	// start computation
	System.gc();
	long a = new Date().getTime();

	if (!nop) {

	    for (int proc_id = 0; proc_id < nprocs; proc_id++) {
		first_row = (M * proc_id) / nprocs + 1;
		last_row  = (M * (proc_id + 1)) / nprocs;
		
		if ((first_row & 1) != 0)
		    t[proc_id] = new sor_first_row_odd(first_row, last_row);
		else
		    t[proc_id] = new sor_first_row_even(first_row, last_row);
		t[proc_id].start();
	    }
	    
	    for (int proc_id = 0; proc_id < nprocs; proc_id++) {
		try {
		    t[proc_id].join();
		} catch (InterruptedException e) {}
	    }
	}

	long b = new Date().getTime();
	
	System.out.println("Sor-" + nprocs + "\t" + Long.toString(b - a));

	// print out results
	float red_sum = 0, black_sum = 0;
	for (int i = 0; i < M+2; i++)
	    for (int j = 0; j < N+1; j++) {
		red_sum += red_[i][j];
		black_sum += black_[i][j];
	    }
	System.out.println("Exiting. red_sum = " + red_sum + ", black_sum = " + black_sum);
    }

    public static void print(String s) {
	System.out.println(Thread.currentThread().getName()+":"+s);
    }

}


class sor_first_row_odd extends Thread {

    int first_row;
    int end;
    int N = Sor.N;
    int M = Sor.M;
    float[][] black_ = Sor.black_;
    float[][] red_ = Sor.red_;


    public sor_first_row_odd (int a, int b) {
	first_row = a;
	end = b;
    }

    public void run() {
	int	i, j, k;

	for (i = 0; i < Sor.iterations; i++) {
	    //Sor.print("iteration A "+i);
	    for (j = first_row; j <= end; j++) {

		for (k = 0; k < N; k++) {

		    black_[j][k] = (red_[j-1][k] + red_[j+1][k] + red_[j][k] + red_[j][k+1]) / (float) 4.0;
		}
		if ((j += 1) > end)
		    break;
		
		for (k = 1; k <= N; k++) {
		    
		    black_[j][k] = (red_[j-1][k] + red_[j+1][k] + red_[j][k-1] + red_[j][k]) / (float) 4.0;
		}
	    }
	    try {
		//Sor.print("barrier 1a - "+System.currentTimeMillis());
		Sor.barrier.barrier();
	    } catch (InterruptedException e) {}
	  
	    for (j = first_row; j <= end; j++) {
		
		for (k = 1; k <= N; k++) {
		    
		    red_[j][k] = (black_[j-1][k] + black_[j+1][k] + black_[j][k-1] + black_[j][k]) / (float) 4.0;
		}
		if ((j += 1) > end)
		    break;
		
		for (k = 0; k < N; k++) {
		    
		    red_[j][k] = (black_[j-1][k] + black_[j+1][k] + black_[j][k] + black_[j][k+1]) / (float) 4.0;
		}
	    }				
	    try {
		//Sor.print("barrier 2a - "+System.currentTimeMillis());
		Sor.barrier.barrier();
	    } catch (InterruptedException e) {}

	}
	
    }

}

class sor_first_row_even extends Thread {   
    
    int first_row;
    int end;
    int N = Sor.N;
    int M = Sor.M;
    float[][] black_ = Sor.black_;
    float[][] red_ = Sor.red_;

    public sor_first_row_even (int a,int b) {
	first_row = a;
	end = b;
	this.N = N;
	this.M = M;
    }

    public void run() {
	int i, j, k;

	for (i = 0; i < Sor.iterations; i++) {
	    //Sor.print("iteration B "+i);	    
	    for (j = first_row; j <= end; j++) {
		
		for (k = 1; k <= N; k++) {
		    
		    black_[j][k] = (red_[j-1][k] + red_[j+1][k] + red_[j][k-1] + red_[j][k]) / (float) 4.0;
		}
		if ((j += 1) > end)
		    break;
		
		for (k = 0; k < N; k++) {
		    
		    black_[j][k] = (red_[j-1][k] + red_[j+1][k] + red_[j][k] + red_[j][k+1]) / (float) 4.0;
		}
	    }
	    try {
		//Sor.print("barrier 1b - "+System.currentTimeMillis());
		Sor.barrier.barrier();
	    } catch (InterruptedException e) {}

	    for (j = first_row; j <= end; j++) {
		
		for (k = 0; k < N; k++) {
		    
		    red_[j][k] = (black_[j-1][k] + black_[j+1][k] + black_[j][k] + black_[j][k+1]) / (float) 4.0;
		}
		if ((j += 1) > end)
		    break;
		
		for (k = 1; k <= N; k++) {
		    
		    red_[j][k] = (black_[j-1][k] + black_[j+1][k] + black_[j][k-1] + black_[j][k]) / (float) 4.0;
		}
	    }				
	    try {
		//Sor.print("barrier 2b - "+System.currentTimeMillis());
		Sor.barrier.barrier();
	    } catch (InterruptedException e) {}

	}
    }
}





