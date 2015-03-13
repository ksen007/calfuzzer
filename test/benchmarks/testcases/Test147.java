package benchmarks.testcases;

public class Test147 {
	 public static Object l19 = new Object();
	 public static Object l4 = new Object();
	 public static Object l20 = new Object();
	 public static Object l8 = new Object();
	 public static boolean c0;
	 public static boolean c1;
	 public static boolean c2;
	 public static boolean c3;
	 public static Thread t17 = new Thread(){
		 public void run(){
			 try{
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				c0 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				c0 = true;
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c2 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c2 = true;
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1048
					synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1786
							c0 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1786
							synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
							c0 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1786
						synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1206
							l19.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1207
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1208
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1061
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c0) { 
					synchronized (l19) { l19.wait(); }
					synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				}
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l8){ //null:-1
				} //null:-1
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1163
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1166
				synchronized(l19){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l8){ //null:-1
				} //null:-1
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1370
					synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1786
							c1 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1786
							synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
							c1 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1786
						synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1206
							l20.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1207
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1208
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1390
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t2 = new Thread(){
		 public void run(){
			 try{
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1625
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1625
				synchronized(l8){ //null:-1
				} //null:-1
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1370
					synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1390
				t17.start(); //null:-1
				t18.start(); //null:-1
				t17.join(); //null:-1
				t18.join(); //null:-1
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t18 = new Thread(){
		 public void run(){
			 try{
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				c1 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				c1 = true;
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c3 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c3 = true;
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1048
					synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1061
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c1) { 
					synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					if (c3) { 
						synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1097
							l20.wait(); //org/apache/commons/pool/impl/GenericObjectPool.java:1100
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1111
					}
				}
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c1) { 
					synchronized (l20) { l20.wait(); }
					synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				}
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l8){ //null:-1
				} //null:-1
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1163
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1166
				synchronized(l20){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l8){ //null:-1
				} //null:-1
				synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1370
					synchronized(l4){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1390
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
