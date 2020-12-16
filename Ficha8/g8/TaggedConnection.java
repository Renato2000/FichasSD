package g8;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
	private final Socket s;
	private final DataInputStream is;
	private final DataOutputStream os;
	private ReentrantLock sendLock = new ReentrantLock();
	private ReentrantLock receiveLock = new ReentrantLock();

	public static class Frame {
		public final int tag;
		public final byte[] data;
		public Frame(int tag, byte[] data) {
			this.tag = tag;
			this.data = data;
		}
	}

	public TaggedConnection(Socket socket) throws IOException {
		this.s = socket;
		this.is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	public void send(Frame frame) throws IOException {
		send(frame.tag, frame.data);
	}

	public void send(int tag, byte[] data) throws IOException {
		this.sendLock.lock();

		try{
			this.os.writeInt(4 + data.length);
			this.os.writeInt(tag);
			this.os.write(data);
			this.os.flush();
		} finally {
			this.sendLock.unlock();
		}
	}

	public Frame receive() throws IOException {
		byte[] data;
		int tag;
		
		this.receiveLock.lock();

		try{
			tag = this.is.readInt();
			data = new byte[this.is.readInt() - 4];	 
			this.is.readFully(data);

			return new Frame(tag, data);
		} finally {
			this.receiveLock.unlock();
		}
	}

	public void close() throws IOException {
		this.s.close();
	}
}