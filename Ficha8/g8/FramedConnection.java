package g8;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/*
	O rwLock não iria servir pq não bloqueia a leitura. Além disso, iria bloquear a leitura quando houvesse uma escrita.
*/

public class FramedConnection implements AutoCloseable {
	private final Socket s;
	private final DataInputStream is;
	private final DataOutputStream os;
	private ReentrantLock sendLock = new ReentrantLock();
	private ReentrantLock receiveLock = new ReentrantLock();

	public FramedConnection(Socket socket) throws IOException { 
		this.s = socket;
		this.is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	public void send(byte[] data) throws IOException {
		this.sendLock.lock();

		try{
			this.os.writeInt(data.length);
			this.os.write(data);
			this.os.flush();
		} finally {
			this.sendLock.unlock();
		}
	}

	public byte[] receive() throws IOException {
	  	byte[] data;
	  	this.receiveLock.lock();

	  	try{
	  		data = new byte[this.is.readInt()];	  		
	  		this.is.readFully(data); //o readFully não vai sair enquanto não preencher o data[]
	  	} finally {
	  		this.receiveLock.unlock();
	  	}

	  	return data;
	}

	public void close() throws IOException {
		 this.s.close();
	}
}