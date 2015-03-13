package benchmarks;

public class AtomicityTest {
	class Transaction extends Thread {
		public Transaction(int n, boolean deposit)
		{
			amount = n;
			isDeposit = deposit;
		}
		private int amount;
		private boolean isDeposit;
		public void run() { 
			if (isDeposit)
				deposit(amount);
			else {
				try {
					withdraw(amount);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Integer balance = 0;
    private Integer lock = 0;


    public int getBalance() {
		return balance.intValue();
	}
	
	public void deposit(int n) {
		synchronized(lock) {
            System.out.println("Updated balance in deposit "+Thread.currentThread());
            System.out.flush();
			balance += n;
		}
	}
	public synchronized void withdraw(int n) throws Exception {
		int old_balance;
		//synchronized(this) {
		synchronized(lock) {
			old_balance = balance;
//            System.out.println("Read balance in withdraw "+Thread.currentThread());
//            System.out.flush();
        }
		//if(old_balance < n) {
		//	return;
		//}
		synchronized(lock) {
//            System.out.println("Updated balance in withdraw "+Thread.currentThread());
//            System.out.flush();
			balance = old_balance - n;
		}
		//if (balance < 0)
		//	throw (new Exception("Negative balance"));
		//}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AtomicityTest acc = new AtomicityTest();
		Thread[] threads = new Thread[10];
		threads[0] = acc.new Transaction(100, false);
		threads[1] = acc.new Transaction(100, true);
		threads[2] = acc.new Transaction(100, false);
		threads[3] = acc.new Transaction(150, true);
		//threads[4] = acc.new Transaction(100, true);
		//threads[5] = acc.new Transaction(150, false);
		//threads[6] = acc.new Transaction(200, true);
		//threads[7] = acc.new Transaction(250, false);
		for(int i = 0; i < 4; i++) {
			threads[i].start();
		}
	}

}
