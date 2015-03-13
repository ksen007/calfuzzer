package benchmarks;

public class Bank {

    private static class Account {
	int balance;
    
	public Account() {
	    balance = 0;
	}
    
	public Account(int init) {
	    balance = init;
	}
    
	public void deposit(int amt) {
	    int old_balance = balance();
	    synchronized (this) {
		balance = old_balance + amt;
	    }
	}
    
	public void withdraw(int amt) {
	    int old_balance = balance();
	    synchronized (this) {
		balance = old_balance + amt;
	    }
	}
    
	public synchronized int balance() {
	    return balance;
	}
    }

    private static Account a = new Account(100);
    
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                a.deposit(100);
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                a.deposit(100);
            }
        };
	t1.start();
	t2.start();
	t1.join();
	t2.join();
	System.out.println(a.balance());
    }

}
