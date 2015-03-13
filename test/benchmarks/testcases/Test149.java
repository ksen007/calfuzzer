package benchmarks.testcases;

public class Test149 {
	 public static Object l5 = new Object();
	 public static Object l11 = new Object();
	 public static Object l12 = new Object();
	 public static boolean c0;
	 public static boolean c1;
	 public static boolean c2;
	 public static boolean c3;
	 public static Thread t2 = new Thread(){
		 public void run(){
			 try{
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1625
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1625
				t9.start(); //null:-1
				t10.start(); //null:-1
				t9.join(); //null:-1
				t10.join(); //null:-1
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t9 = new Thread(){
		 public void run(){
			 try{
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				c0 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				c0 = true;
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c2 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c2 = true;
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1048
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							c2 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							c2 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1803
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1223
							l11.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1224
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1225
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1061
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c0) { 
					synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					if (c2) { 
						synchronized (l11) { l11.wait(); }
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					}
				}
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1786
					c0 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1786
					synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					c0 = false;
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1786
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1176
					synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1812
						c0 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1812
						c2 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1813
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
						c0 = true;
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						c2 = true;
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1812
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							c2 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							c2 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1803
						synchronized(l11){ //org/apache/commons/pool/impl/GenericObjectPool.java:1223
							l11.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1224
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1225
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1181
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1245
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							c3 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							c3 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1803
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1223
							l12.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1224
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1225
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1248
			}
			catch(Exception e){
				 System.out.println("Exception caught in run");
				 e.printStackTrace();
			}
		}
	};
	 public static Thread t10 = new Thread(){
		 public void run(){
			 try{
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				c1 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				c1 = true;
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c3 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1750
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
				c3 = true;
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1048
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1061
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c1) { 
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					if (c3) { 
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1097
							l12.wait(); //org/apache/commons/pool/impl/GenericObjectPool.java:1100
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1111
					}
				}
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1064
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1066
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				if (c1) { 
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					if (c3) { 
						synchronized (l12) { l12.wait(); }
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
					}
				}
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1786
					c1 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1786
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
					c1 = false;
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1786
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1176
					synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1812
						c1 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1812
						c3 = true; //org/apache/commons/pool/impl/GenericObjectPool.java:1813
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1778
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1778
						c1 = true;
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
						c3 = true;
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1812
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							c3 = false; //org/apache/commons/pool/impl/GenericObjectPool.java:1803
							synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							} //org/apache/commons/pool/impl/GenericObjectPool.java:1795
							c3 = false;
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1803
						synchronized(l12){ //org/apache/commons/pool/impl/GenericObjectPool.java:1223
							l12.notify(); //org/apache/commons/pool/impl/GenericObjectPool.java:1224
						} //org/apache/commons/pool/impl/GenericObjectPool.java:1225
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1181
				synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1245
					synchronized(l5){ //org/apache/commons/pool/impl/GenericObjectPool.java:1198
					} //org/apache/commons/pool/impl/GenericObjectPool.java:1198
				} //org/apache/commons/pool/impl/GenericObjectPool.java:1248
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
