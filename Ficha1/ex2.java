class Deposito implements Runnable{
	private Bank banco;

	public Deposito(Bank b){
		this.banco = b;
	}

	public void run(){
		for(int i = 0; i < 1000; i++){
			this.banco.deposit(100);
		}
	}
}


public class ex2{
	public static void main(String args[]) throws Exception{
		int nThreads = 10;
		Bank banco = new Bank();
		Deposito dep = new Deposito(banco);
		Thread threads[] = new Thread[nThreads];

		for(int i = 0; i < nThreads; i++){
			threads[i] = new Thread(new Deposito(banco));
		}

		for(int i = 0; i < nThreads; i++){
			threads[i].start();
		}

		for(int i = 0; i < nThreads; i++){
			threads[i].join();
		}

		System.out.println("Valor final: " + banco.balance());
	}
}