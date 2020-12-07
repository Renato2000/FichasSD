import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.locks.ReentrantLock;

class ContactList {
    private List<Contact> contacts;
    private ReentrantLock lock = new ReentrantLock();

    public ContactList() {
        contacts = new ArrayList<>();

        contacts.add(new Contact("John", 20, 253123321, null, new ArrayList<>(Arrays.asList("john@mail.com"))));
        contacts.add(new Contact("Alice", 30, 253987654, "CompanyInc.", new ArrayList<>(Arrays.asList("alice.personal@mail.com", "alice.business@mail.com"))));
        contacts.add(new Contact("Bob", 40, 253123456, "Comp.Ld", new ArrayList<>(Arrays.asList("bob@mail.com", "bob.work@mail.com"))));
    }

    public void addContact(Contact c) {
        lock.lock();
        try{
            contacts.add(c);
        }
        finally{
            lock.unlock();
        }
    }

    public ContactListValueObject getContacts() { 
        lock.lock();
        try{
        	//Isto só é valido porque a contactList é imutável.
        	return new ContactListValueObject(new ArrayList<>(this.contacts));
        	//return new ArrayList<>(this.contacts);
        }
        finally{
            lock.unlock();
        }
    }
    
}

class ClientWorker implements Runnable {
    private Socket socket;
    private ContactList contactList;

    public ClientWorker (Socket socket, ContactList contactList) {
        this.socket = socket;
        this.contactList = contactList;
    }

    @Override
    public void run() {
        try{
            socket.shutdownOutput();
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            boolean isOpen = true;

            while(isOpen){
                Contact newContact = Contact.deserialize(in);
                System.out.println(newContact);
                
                this.contactList.addContact(newContact);
            }

            socket.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientListener implements Runnable{
    private ServerSocket serverSocket;
    private ContactList contactList;

    public ClientListener(ServerSocket socket, ContactList contactList){
        this.serverSocket = socket;
        this.contactList = contactList;
    }

    public void run(){
        try{
            while (true) {
            	System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Connected...");
                Thread worker = new Thread(new ClientWorker(socket, contactList)); // Vai criar um thread para tratar cada um dos clientes
                worker.start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}

class BackupWorker implements Runnable {
    private Socket socket;
    private ContactList contactList;

    public BackupWorker (Socket socket, ContactList contactList) {
        this.socket = socket;
        this.contactList = contactList;
    }

    @Override
    public void run() {
        try{
            socket.shutdownInput(); // Como o input não é necessário, podemos fecha-lo
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            /*
            List<Contact> contactList = this.contactList.getContacts();            

            out.writeInt(contactList.size());
            for(Contact c : contactList){
                c.serialize(out);
            }
            */

			ContactListValueObject contactList = this.contactList.getContacts();
			contactList.serialize(out);

            socket.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class BackupListener implements Runnable{
    private ServerSocket serverSocket;
    private ContactList contactList;

    public BackupListener(ServerSocket socket, ContactList contactList){
        this.serverSocket = socket;
        this.contactList = contactList;
    }

    public void run(){
        try{
            while (true) {
                Socket socket = serverSocket.accept();
                Thread worker = new Thread(new BackupWorker(socket, contactList));
                worker.start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}

/*
	A main thread será responsável por tratar dos clientes de backup
*/
public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345); //socket dedicado aos clientes normais
        ServerSocket backupServerSocket = new ServerSocket(23456); //socket dedicado ao cliente de backup
        ContactList contactList = new ContactList();

        Thread clientListener = new Thread(new ClientListener(serverSocket, contactList));
        BackupListener backupListener = new BackupListener(backupServerSocket, contactList);

        clientListener.start();
        backupListener.run();

        try{
        	clientListener.join();
    	}
    	catch(Exception e){

    	}
    }

}
