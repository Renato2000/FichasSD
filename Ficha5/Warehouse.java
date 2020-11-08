import java.util.*;
import java.util.concurrent.locks.*;

/*
Esta versão é considerada egoísta, uma vez clientes tentam apropriar-se dos items o mais rapido possível.
Se o cliente não tiver os items disponíveis, os outros que tenham os items disponiveis vão ficar à espera.
Um cliente irá atrasar todos os outros.
*/
class Warehouse {
  	private Lock l = new ReentrantLock();
  	private Map<String, Product> m =  new HashMap<String, Product>();

  	private class Product { 
  		int q = 0; 
  		/*
  		Para que threads não acordem desnecessariamente, podemos colocar uma condição em cada produto, o que 
  		garante que apenas threads que estejam interessadas neste produto é que vão acordar.
  		*/
  		Condition isEmpty = l.newCondition();
  	}

 	private Product get(String s) {
	    Product p = m.get(s);
 	   	if (p != null) return p;
 	   	p = new Product();
 	    m.put(s, p);
  	    return p;
  	}

  	public void supply(String s, int q) {
  		l.lock();
  	  	try{
  	  	  	Product p = get(s);
  	  	  	p.q += q;
  	  	  	p.isEmpty.signalAll();
  	  	}
  		finally{ l.unlock(); }
  	}

  	public void consume(String[] a) throws InterruptedException {
  		l.lock();
  		try{
  		   	for (String s : a){
  		   		Product p = get(s);
  		  		while(p.q == 0) {
  		  			System.out.println(Thread.currentThread().getName() + " waiting for item" + s);
  		  			p.isEmpty.await();
  		  		} 
  		  		p.q--;
  		  		System.out.println(Thread.currentThread().getName() + " got item " + s);
  		  	}
  		}
  		finally { l.unlock(); }
  	}
}

//Versão cooperativa
/*
Neste caso pode acontecer starvation, ou seja, um cliente pode ficar indefinidamente à espera se aparecerem sempre clientes com uma lista menor.
Para corrigir este problema, temos de criar um ponto de tolerancia, ou seja, só deixamos passar um certo número de clientes até passar da abordagem
cooperativa à abordagem egoista.
*/
class WarehouseV2 {
  	private Lock l = new ReentrantLock();
  	private Map<String, Product> m =  new HashMap<String, Product>();

  	private class Product { 
  		int q = 0; 
  		/*
  		Para que threads não acordem desnecessariamente, podemos colocar uma condição em cada produto, o que 
  		garante que apenas threads que estejam interessadas neste produto é que vão acordar.
  		*/
  		Condition isEmpty = l.newCondition();
  	}

 	private Product get(String s) {
	    Product p = m.get(s);
 	   	if (p != null) return p;
 	   	p = new Product();
 	    m.put(s, p);
  	    return p;
  	}

  	public void supply(String s, int q) {
  		l.lock();
  	  	try{
  	  	  	Product p = get(s);
  	  	  	p.q += q;
  	  	  	p.isEmpty.signalAll();
  	  	}
  		finally{ l.unlock(); }
  	}

  	public void consume(String[] a) throws InterruptedException {
  		l.lock();
  		try{
  			//1. Verificação
  			int i = 0;
  			while(i < a.length){
  		   		Product p = get(a[i]);

  		   		i++;
  				while(p.q == 0) {
  		  			System.out.println(Thread.currentThread().getName() + " waiting for item" + a[i]);
  		  			p.isEmpty.await();
  		  			i=0; //Como algo pode ter alterado depois do await, temos de verificar de novo 
  		  		}
  			}
  			//2. Consumo
  			//Só chegamos a esta fase se nunca cairmos no await, ou seja, se todos os produtos estiverem disponiveis
  			System.out.println(Thread.currentThread().getName() + " getting items");
  		   	for (String s : a){
  		   		Product p = get(s);
  		  		p.q--;
  		  	}
  		}
  		finally { l.unlock(); }
  	}
}

/*
Versão com um ponto de tolerancia, onde o cliente só deixa passar um certo nr de clientes até passar à abordagem egoista.
*/
class WarehouseV3 {
  	private Lock l = new ReentrantLock();
  	private Map<String, Product> m =  new HashMap<String, Product>();
  	private int max = 10; //Nr maximo de clientes que deixa passar à frente

  	private class Product { 
  		int q = 0; 
  		/*
  		Para que threads não acordem desnecessariamente, podemos colocar uma condição em cada produto, o que 
  		garante que apenas threads que estejam interessadas neste produto é que vão acordar.
  		*/
  		Condition isEmpty = l.newCondition();
  	}

 	private Product get(String s) {
	    Product p = m.get(s);
 	   	if (p != null) return p;
 	   	p = new Product();
 	    m.put(s, p);
  	    return p;
  	}

  	public void supply(String s, int q) {
  		l.lock();
  	  	try{
  	  	  	Product p = get(s);
  	  	  	p.q += q;
  	  	  	p.isEmpty.signalAll();
  	  	}
  		finally{ l.unlock(); }
  	}

  	public void consume(String[] a) throws InterruptedException {
  		l.lock();
  		try{
  			//1. Verificação
  			int i = 0;
  			int n = 0;
  			while(i < a.length && n < max){
  		   		Product p = get(a[i]);

  		   		i++;
  				while(p.q == 0) {
  		  			System.out.println(Thread.currentThread().getName() + " waiting for item" + a[i]);
  		  			p.isEmpty.await();
  		  			i=0; //Como algo pode ter alterado depois do await, temos de verificar de novo
  		  			n++;
  		  		}
  			}
  			if(n < max){ //Continua com a abordagem cooperativa
  				//2. Consumo
  				//Só chegamos a esta fase se nunca cairmos no await, ou seja, se todos os produtos estiverem disponiveis
  				System.out.println(Thread.currentThread().getName() + " getting items");
  		   		for (String s : a){
  		   			Product p = get(s);
  		  			p.q--;

  				}
  		  	}
  		  	else{ //Passa para a abordagem egoista
  		   		for (String s : a){
  		   			Product p = get(s);
  		  			while(p.q == 0) {
  		  				System.out.println(Thread.currentThread().getName() + " waiting for item" + s);
  		  				p.isEmpty.await();
  		  			} 
  		  			p.q--;
  		  			System.out.println(Thread.currentThread().getName() + " got item " + s);
  		  		}
  		  	}
  		}
  		finally { l.unlock(); }
  	}
}

class Consumer implements Runnable{
	private String[] items;
	private Warehouse w;

	public Consumer(String[] items, Warehouse w){
		this.items = items;
		this.w = w;
	}

	public void run(){
		try{
			w.consume(items);
		}
		catch(Exception e) {}
	}
}

class Producer implements Runnable{
	private Warehouse w;
	private String item;
	private int qty;

	public Producer(Warehouse w, String item, int qty){
		this.w = w;
		this.item = item;
		this.qty = qty;
	}

	public void run(){
		try{
			w.supply(item, qty);
		}
		catch(Exception e) {}		
	}
}

class MainWarehouse{
	public static void main(String[] args) throws InterruptedException{
		Warehouse w = new Warehouse();

		Thread consumer1 = new Thread(new Consumer(new String[]{"item1"}, w));
		consumer1.setName("consumer1");


		Thread consumer2 = new Thread(new Consumer(new String[]{"item2"}, w));
		consumer2.setName("consumer2");

		Thread producer = new Thread(new Producer(w, "item1", 1));
		producer.setName("producer");

		consumer1.start();
		consumer2.start();
		Thread.sleep(1000); //force producer delay
		producer.start();

		consumer1.join();
		consumer2.join();
		producer.join();
	}
}