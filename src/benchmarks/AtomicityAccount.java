package benchmarks;

public class AtomicityAccount {

    private static class Account {
	private int bal = 150;
	public synchronized int getBalance() {
	    return bal;
	}
	public synchronized void withdraw(int amount) {
	    bal = bal - amount;
	}
    }
    
    static Account acnt1 = new Account();
    static Account acnt2 = new Account();
    
    public static void main(String[] args) throws InterruptedException {

	f1();
    
	Thread t = new Thread() {
		public void run(){
		    try { Thread.sleep(1000); } catch (Exception e) {}
		    f3();
		    f4();
		}
	    };
	t.start();
	f2();
	t.join();
    }

    private static void f1() {
	if (acnt2.getBalance() >= 70) {
	    System.out.println("f1");
	    acnt2.withdraw(70);        
	}
    }

    private static void f2() {
	if (acnt2.getBalance() >= 70) {
	    System.out.println("f2");
	    acnt2.withdraw(70);
	}
    }

    private static void f3() {
	if (acnt1.getBalance() >= 70) {
	    System.out.println("f3");
	    acnt1.withdraw(70);        
	}
    }

    private static void f4() {
	if (acnt2.getBalance() >= 0) {
	    System.out.println("f4");
	    acnt2.withdraw(acnt2.getBalance());        
	}
    }

}