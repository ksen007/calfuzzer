package benchmarks.testcases;


public class TestLogInfo {
	 public static Object l51 = new Object();
	 public static Object l65 = new Object();
	 public static Object l22 = new Object();
	 public static Object l56 = new Object();
	 public static Object l61 = new Object();
	 public static Object l44 = new Object();
	 public static Object l60 = new Object();
	 public static Object l45 = new Object();
	 public static boolean c0;
	 public static boolean c1;
	 public static Thread t2 = new Thread(){
		 public void run(){
			 try{
				synchronized(l44){ //org/apache/log4j/Hierarchy.java:267
				} //org/apache/log4j/Hierarchy.java:274
				synchronized(l51){ //org/apache/log4j/FileAppender.java:-1
				} //org/apache/log4j/FileAppender.java:-1
				synchronized(l56){ //org/apache/log4j/FileAppender.java:-1
				} //org/apache/log4j/FileAppender.java:-1
				c0 = true; //org/apache/log4j/helpers/BoundedFIFO.java:40
				c1 = true; //org/apache/log4j/helpers/BoundedFIFO.java:47
				c0 = false; //org/apache/log4j/helpers/BoundedFIFO.java:63
				t62.start(); //org/apache/log4j/AsyncAppender.java:71
				synchronized(l61){ //org/apache/log4j/helpers/BoundedFIFO.java:-1
					c0 = false;
				} //org/apache/log4j/helpers/BoundedFIFO.java:-1
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:76
				} //org/apache/log4j/AsyncAppender.java:78
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:76
				} //org/apache/log4j/AsyncAppender.java:78
				synchronized(l45){ //org/apache/log4j/Category.java:-1
				} //org/apache/log4j/Category.java:-1
				synchronized(l45){ //org/apache/log4j/Category.java:201
					synchronized(l60){ //org/apache/log4j/AppenderSkeleton.java:-1
						synchronized(l61){ //org/apache/log4j/AsyncAppender.java:101
							if (c0) { 
								synchronized (l61) { l61.wait(); }
							}
							c1 = false; //org/apache/log4j/helpers/BoundedFIFO.java:99
							c0 = false;
							l61.notify(); //org/apache/log4j/AsyncAppender.java:121
						} //org/apache/log4j/AsyncAppender.java:123
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/Category.java:208
				synchronized(l22){ //org/apache/log4j/Category.java:201
				} //org/apache/log4j/Category.java:208
				synchronized(l44){ //org/apache/log4j/Hierarchy.java:267
				} //org/apache/log4j/Hierarchy.java:276
				t82.start(); //TestLoggingDet.java:95
				t83.start(); //TestLoggingDet.java:102
				t82.join(); //TestLoggingDet.java:107
				t83.join(); //TestLoggingDet.java:114
				synchronized(l51){ //org/apache/log4j/WriterAppender.java:-1
				} //org/apache/log4j/WriterAppender.java:-1
				synchronized(l56){ //org/apache/log4j/WriterAppender.java:-1
				} //org/apache/log4j/WriterAppender.java:-1
				synchronized(l60){ //org/apache/log4j/AsyncAppender.java:132
				} //org/apache/log4j/AsyncAppender.java:138
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:276
					if (c1) { 
						l61.notify(); //org/apache/log4j/AsyncAppender.java:282
					}
				} //org/apache/log4j/AsyncAppender.java:285
				t62.join(); //org/apache/log4j/AsyncAppender.java:146
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t83 = new Thread(){
		 public void run(){
			 try{
				synchronized(l61){ //org/apache/log4j/helpers/BoundedFIFO.java:-1
					c0 = false;
				} //org/apache/log4j/helpers/BoundedFIFO.java:-1
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t82 = new Thread(){
		 public void run(){
			 try{
				synchronized(l45){ //org/apache/log4j/Category.java:201
					synchronized(l60){ //org/apache/log4j/AppenderSkeleton.java:-1
						synchronized(l61){ //org/apache/log4j/AsyncAppender.java:101
							if (c0) { 
								synchronized (l61) { l61.wait(); }
							}
							c0 = true; //org/apache/log4j/helpers/BoundedFIFO.java:99
							c0 = true;
						} //org/apache/log4j/AsyncAppender.java:123
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/Category.java:208
				synchronized(l22){ //org/apache/log4j/Category.java:201
				} //org/apache/log4j/Category.java:208
				synchronized(l45){ //org/apache/log4j/Category.java:201
					synchronized(l60){ //org/apache/log4j/AppenderSkeleton.java:-1
						synchronized(l61){ //org/apache/log4j/AsyncAppender.java:101
							if (c0) {
								l61.wait(); //org/apache/log4j/AsyncAppender.java:106
							}
							c1 = false; //org/apache/log4j/helpers/BoundedFIFO.java:99
							c0 = false;
							l61.notify(); //org/apache/log4j/AsyncAppender.java:121
						} //org/apache/log4j/AsyncAppender.java:123
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/Category.java:208
				synchronized(l22){ //org/apache/log4j/Category.java:201
				} //org/apache/log4j/Category.java:208
				synchronized(l45){ //org/apache/log4j/Category.java:201
					synchronized(l60){ //org/apache/log4j/AppenderSkeleton.java:-1
						synchronized(l61){ //org/apache/log4j/AsyncAppender.java:101
							if (c0) { 
								synchronized (l61) { l61.wait(); }
							}
							c0 = true; //org/apache/log4j/helpers/BoundedFIFO.java:99
							c0 = true;
						} //org/apache/log4j/AsyncAppender.java:123
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/Category.java:208
				synchronized(l22){ //org/apache/log4j/Category.java:201
				} //org/apache/log4j/Category.java:208
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t62 = new Thread(){
		 public void run(){
			 try{
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
						synchronized (l61) { l61.wait(); }
					}
					if (c0) { 
						c0 = false; //org/apache/log4j/helpers/BoundedFIFO.java:82
						c1 = false;
						l61.notify(); //org/apache/log4j/AsyncAppender.java:335
					}
				} //org/apache/log4j/AsyncAppender.java:338
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:342
					synchronized(l51){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
					synchronized(l56){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/AsyncAppender.java:346
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
						synchronized (l61) { l61.wait(); }
					}
					if (c0) { 
						synchronized (l61) { l61.notify(); }
						c1 = true; //org/apache/log4j/helpers/BoundedFIFO.java:82
						c1 = true;
					}
				} //org/apache/log4j/AsyncAppender.java:338
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:342
					synchronized(l51){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
					synchronized(l56){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/AsyncAppender.java:346
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
						l61.wait(); //org/apache/log4j/AsyncAppender.java:321
					}
					if (c0) { 
						c0 = false; //org/apache/log4j/helpers/BoundedFIFO.java:82
						c1 = false;
						l61.notify(); //org/apache/log4j/AsyncAppender.java:335
					}
				} //org/apache/log4j/AsyncAppender.java:338
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:342
					synchronized(l51){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
					synchronized(l56){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/AsyncAppender.java:346
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
						synchronized (l61) { l61.wait(); }
					}
					if (c0) { 
						synchronized (l61) { l61.notify(); }
						c1 = true; //org/apache/log4j/helpers/BoundedFIFO.java:82
						c1 = true;
					}
				} //org/apache/log4j/AsyncAppender.java:338
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:342
					synchronized(l51){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
					synchronized(l56){ //org/apache/log4j/AppenderSkeleton.java:-1
					} //org/apache/log4j/AppenderSkeleton.java:-1
				} //org/apache/log4j/AsyncAppender.java:346
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
						l61.wait(); //org/apache/log4j/AsyncAppender.java:321
					}
					if (c0) { 
						synchronized (l61) { l61.notify(); }
					}
				} //org/apache/log4j/AsyncAppender.java:338
				synchronized(l65){ //org/apache/log4j/AsyncAppender.java:342
				} //org/apache/log4j/AsyncAppender.java:346
				synchronized(l61){ //org/apache/log4j/AsyncAppender.java:310
					if (c1) { 
					}
				} //org/apache/log4j/AsyncAppender.java:317
				synchronized(l51){ //org/apache/log4j/WriterAppender.java:-1
				} //org/apache/log4j/WriterAppender.java:-1
				synchronized(l56){ //org/apache/log4j/WriterAppender.java:-1
				} //org/apache/log4j/WriterAppender.java:-1
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
