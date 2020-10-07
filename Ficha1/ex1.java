public class ex1{
	public static void main(String args[]) throws Exception{
		int nThreads = 10;

		Thread threads[] = new Thread[nThreads];

		for(int i = 0; i < nThreads; i++){
			threads[i] = new Thread(new Incrementer());
		}

		for(int i = 0; i < nThreads; i++){
			threads[i].start();
		}

		for(int i = 0; i < nThreads; i++){
			threads[i].join();
		}

		System.out.println("Done!");
	}
}

/*
Como cada thread tem a sua propria stack, o uso de um mesmo Incrementer em todas
as threads não causará problemas, já que cada thread terá a sua variável.
No entanto, se a variável que é alterada não for local, isto já será um problema,
porque as varias threads vão tentar aceder à mesma variável.
*/