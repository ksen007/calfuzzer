package benchmarks.testcases;


public class Test1890 {
	 public static Object l11 = new Object();
	 public static Object l14 = new Object();
	 public static boolean c0;
	 public static Thread t2 = new Thread(){
		 public void run(){
			 try{
				c0 = false;
				t5.start(); //null:-1
				t6.start(); //null:-1
				t5.join(); //null:-1
				t6.join(); //null:-1
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t5 = new Thread(){
		 public void run(){
			 try{
				synchronized(l11){ //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:346
					c0 = true; //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:347
				} //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:348
				//Thread.sleep(1000);
				synchronized(l11){ //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:353
					c0 = false; //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:354
				} //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:355
				synchronized(l14){ //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:356
					l14.notify(); //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:357
				} //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:358
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t6 = new Thread(){
		 public void run(){
			 try{
				synchronized(l11){ //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:142
					synchronized(l14){ //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:321
						if (c0) { 
							synchronized (l14) { l14.wait(); }
						}
					} //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:329
				} //org/codehaus/groovy/runtime/metaclass/MemoryAwareConcurrentReadMap.java:145
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static void main(String[] args){
		t2.start();
	 }
}
