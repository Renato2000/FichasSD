import java.util.concurrent.locks.ReentrantLock;

class Bank {

 	private static class Account {
    	private int balance;
    	public ReentrantLock lock = new ReentrantLock();
    
    	Account(int balance) {
    		this.balance = balance;
    	}
    
    	int balance() {
    		this.lock.lock();
    		try{
    			return balance;
    		}
    		finally{
    			this.lock.unlock();
    		}
    	}
    
    	boolean deposit(int value) {
    		this.lock.lock();
    		try{
    			balance += value;
    			return true;
    		}
    	  	finally{
    	  		this.lock.unlock();
    	  	}
    	}
    
    	boolean withdraw(int value) {
    		this.lock.lock();
    		try{
    			if (value > balance) return false;
      			balance -= value;
      			return true;
    		}
    		finally{
    			this.lock.unlock();
    		}
    	}
  	}

  	// Bank slots and vector of accounts
  	private int slots;
  	private Account[] av;

  	public Bank(int n){
    	slots = n;
    	av = new Account[slots];
  	  	for (int i=0; i<slots; i++) av[i] = new Account(0);
  	}

  	// Account balance
  	public int balance(int id) {
    	if (id < 0 || id >= slots)
      		return 0;

    	return av[id].balance();
  	}

  	// Deposit
  	boolean deposit(int id, int value) {
    	if (id < 0 || id >= slots)
        	return false;

    	return av[id].deposit(value);
  	}

  	// Withdraw; fails if no such account or insufficient balance
 	public boolean withdraw(int id, int value) {
    	if (id < 0 || id >= slots)
      		return false;

    	return av[id].withdraw(value);
  	}

  	/*
	Apesar de o withraw e o deposit serem duas operações atomicas, a sua composição não será uma operação atómica.

	Irá acontecer deadlock. Se a conta 1 bloquear a conta 2, e a conta 2 bloquear a conta 1, quando a conta 1 tentar ganhar o seu lock, não vai consegui, uma vez que
	a conta 2 possui o lock, acontecendo o mesmo para a conta 2.
	Podemos corrigir este deadlock bloqueando a conta com o menor indice primeiro. (e desbloqueando tambem) 
  	*/
  	public boolean transfer(int from, int to, int value){
  		if(av[from].balance() < value) return false;
  		
  		int min = Math.min(from, to);
  		int max = Math.max(from, to);

  		av[min].lock.lock();
  		av[max].lock.lock();

  		try{
  			this.withdraw(from, value);
  			this.deposit(to, value);
  			
  			return true;
  		}
  		finally{
  			av[min].lock.unlock();
  			av[max].lock.unlock();
  		}
  	}

  	public int totalBalance(){
  		int sum = 0;

	  	for(int i = 0; i < slots; i++){
	  		av[i].lock.lock();
  			sum += av[i].balance();
  		}

  		for(int i = 0; i < slots; i++){
	  		av[i].lock.unlock();
  		}

  		return sum;
  	}
}
