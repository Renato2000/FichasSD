import java.util.concurrent.locks.*;

class Barrier{
	private int n;
	private int currentThreads;
	private int epoch;
	private Lock lock = new ReentrantLock();
	private Condition waitingForLastThread = lock.newCondition();

	public Barrier (int n){
		this.n = n;
		this.currentThreads = 0;
		this.epoch = 0;
	}

	/*
	Vamos organizar as threads por grupos
	Sempre que a ultima thread de cada grupo, ela incrementa o identificador do grupo, e avisa todas do seu grupo que podem sair.
	Isto impede que threads que não pertencam a um certo grupo, arranquem com ele.
	*/
	public void await() throws InterruptedException{
		lock.lock();


		try{
			int myEpoch = this.epoch; //Como a stack não é partilhada entre as threads, cada thread vai saber a sua epoca. 
									  //Estas variáveis são guardadas na stack, ao contrario das variáveis de instancia.
			currentThreads++;
			
			if(this.currentThreads < this.n){
				while(myEpoch == this.epoch){
					System.out.println("Thread is now waiting...");
					waitingForLastThread.await(); //o await desdobra-se em unlock, espera e lock
				}
			} 
			else{
				this.epoch++;
				this.currentThreads = 0;
				System.out.println("Thread is signalling others...");
				waitingForLastThread.signalAll(); //para evitar estar smp a acordar as threads
			}

			/* 
			//Isto não funciona porque pode entrar uma thread nova mais rapido do que outra que está a dormir.
			//Esta nova thread vai incrementar o exitingThreads e pode entrar no if e colocar o currentThreads a 0, evitando que as threads no while saiam.

			this.exitingThreads++;
			if(this.exitingThreads == this.n){
				this.currentThreads = 0;
			}
			*/

			System.out.println("Thread is now exiting...");
		} finally{
			lock.unlock();
		}
	}
}

class BarrierWorker implements Runnable{
	private Barrier barrier;

	public BarrierWorker(Barrier barrier){
		this.barrier = barrier;
	}

	public void run(){
		try{
			this.barrier.await();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}

class OneShotBarrier{
	public static void main(String[] args){
		int n = 10;
		Thread threads[] = new Thread[n];
		Barrier barrier = new Barrier(10);

		for(int i = 0; i < 10; i++){
			threads[i] = new Thread(new BarrierWorker(barrier));
		}

		for(int i = 0; i < 10; i++){
			threads[i].start();
		}

		for(int i = 0; i < 10; i++){
			try{
				threads[i].join();
			}
			catch(Exception e) {}
		}
	}
}