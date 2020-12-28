package g8;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {
	private final g8.TaggedConnection conn;
	private final Map<Integer, Entry> buffer = new HashMap<>();
	private final Lock lock = new ReentrantLock();
	private Exception exception;
	
	private class Entry{
		final ArrayDeque<byte[]> queue = new ArrayDeque<>();
		final Condition cond = lock.newCondition();
	}

	public Demultiplexer(g8.TaggedConnection taggedConnection) {
		this.conn = taggedConnection; 
	}

	private Entry get(int tag) {
		Entry e = buffer.get(tag);
		if(e == null) {
			e = new Entry();
			buffer.put(tag, e);
		}
		return e;
	}

	public void start() {
		new Thread(() -> { //nesta notação, as threads vão partilhar as variáveis criadas no metodo start
			try{
				while(true) {

					// 1. ler frame da connection
					g8.TaggedConnection.Frame frame = this.conn.receive();

					//Este lock pode estar depois do receive, pq só temos uma thread a ler o receive, e o seu conteudo está protegido
					lock.lock();

					try {
						// 2. ler tag da frame, para obter a entrada correspondente e inserir na queue correspondente
						Entry e = get(frame.tag);
						e.queue.add(frame.data);

						// 3, notificar thread que está a aguardar por mensagens
						e.cond.signal();
					} finally { lock.unlock(); }
				}
			}
			catch(IOException e) { //caso acontece algum erro no socket (tipicamente IOException's)
				//sinalizar todas as threads
				lock.lock(); //é necessário adequirir o lock para usar a variável de instancia
				this.exception = e; //é necesário ficar dentro do lock, porque é um estado partilhado, e evita que novas threads entrem no while
				buffer.forEach((k, v) -> v.cond.signalAll());
				lock.unlock();

				System.out.println("Something went wrong");
			}
		}).start();
	} 

	public void send(g8.TaggedConnection.Frame frame) throws IOException {
		this.conn.send(frame);
	}

	public void send(int tag, byte[] data) throws IOException {
		this.conn.send(tag, data);
	}

	public byte[] receive(int tag) throws Exception {
		lock.lock();

		try {
			// 1. obtém entrada correspondente à tag
			Entry e = get(tag);

			// 2. enquanto mão houver mensagens para ler da queue dessa entrada, bloqueia
			while(e.queue.isEmpty() && this.exception == null) {
				e.cond.await();
			}

			//2.1. verificar se há mensagens para ler...
			if(!e.queue.isEmpty()){
				return e.queue.poll();
			}

			//2.2 verificar o erro...
			throw this.exception;
		} finally { lock.unlock(); }
	}

	public void close() throws IOException {
		this.conn.close();
	}

	/*
	* Para o exercicio adicional, a ideia seria cada thread ler mensagens até aparecer uma mensagem para ele.
	* Enquanto vai lendo mensagens que não são deles, vai guardand essas mensagens no buffer correto.
	* Desta forma, quando a proxima thread for ler, vai ver se tem alguma mensagem no seu buffer, e se tiver
	* recebe essa mensagem, se não tiver, faz o processo descrito anteriormente.
	* */
}