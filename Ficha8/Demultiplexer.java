package g8;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Demultiplexer implements AutoCloseable {
	private final TaggedConnection conn;
	private final Map<Interger, Entry> buffer = new HashMap<>();
	private final ReentrantLock lock = new ReentrantLock();
	private Exception exception;
	
	private class Entry{
		final ArrayDeque<byte[]> queue = new ArrayDeque<>();
		final Condition cond = new lock.newCondition(); 
	}

	public Demultiplexer(TaggedConnection taggedConnection) {
		this.conn = taggedConnection; 
	}

	private Entry get(int tag) {
		Entry e = buffer.get(tag);
		if(e == null) {
			e = new Entry();
			buffer.put(tag, e);
		}
	}

	public void start() {
		new Thread(() -> { //nesta notação, as threads vão partilhar as variáveis criadas no metodo start
			try{
				while(true) {

					// 1. ler frame da connection
					Frame frame = this.conn.receive();

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
			catch(IOException e) {
				//sinalizar todas as threads
				lock.lock(); //é necessário adequiri o lock para usar a variável de instancia
				this.exception = e; //é melhor ficar dentro do lock, porque é um estado partilhado, e evita que novas threads entrem no while
				buffer.forEach((k, v) -> v.cond.signalAll());
				lock.unlock();

				System.out.println("Something went wrong");
			}
		}).start();
	} 

	public void send(Frame frame) throws IOException {
		this.conn.send(frame);
	}

	public void send(int tag, byte[] data) throws IOException {
		this.conn.send(tag, data);
	}

	public byte[] receive(int tag) throws IOException, InterruptedException {
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
}