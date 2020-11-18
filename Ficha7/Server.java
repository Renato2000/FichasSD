import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

class ContactList {
    private List<Contact> contacts;
    private ReentrantLock lock = new ReentrantLock();

    public ContactList() {
        contacts = new ArrayList<>();

        contacts.add(new Contact("John", 20, 253123321, null, new ArrayList<>(Arrays.asList("john@mail.com"))));
        contacts.add(new Contact("Alice", 30, 253987654, "CompanyInc.", new ArrayList<>(Arrays.asList("alice.personal@mail.com", "alice.business@mail.com"))));
        contacts.add(new Contact("Bob", 40, 253123456, "Comp.Ld", new ArrayList<>(Arrays.asList("bob@mail.com", "bob.work@mail.com"))));
    }

    // @TODO
    public void addContact(Contact c) {
        lock.lock();
        try{
            contacts.add(c)
        }
        finally{
            lock.unlock();
        }
    }

    // @TODO
    public void getContacts(DataOutputStream out) throws IOException { 
        lock.lock();
        try{
            out.writeInt(contact.size());
            for(Contact c : contacts){
                c.serialize(out);
            }
        }
        finally{
            lock.unlock();
        }
    }
    
}

class ServerWorker implements Runnable {
    private Socket socket;
    private ContactList contactList;

    public ServerWorker (Socket socket, ContactList contactList) {
        this.socket = socket;
        this.contactList = contactList
    }

    // @TODO
    @Override
    public void run() {
        try{
            socket.shutdownOutput();
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            boolean isOpen = true;

            while(isOpen){
                Contact newContact = Contact.deserialize(in);
                System.out.println(newContact);
                

            }

            socket.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientListener implements Runnable{
    private ServerSocket socket;
    private ContactList contactList;

    public ClientListener(ServerSocket socket, ContactList contactList){
        this.socket = socket;
        this.contactList = contactList;
    }

    public void run(){
        try{
            while (true) {
                Socket socket = serverSocket.accept();
                Thread worker = new Thread(new ServerWorker(socket, contactList));
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

    public ServerWorker (Socket socket, ContactList contactList) {
        this.socket = socket;
        this.contactList = contactList
    }

    // @TODO
    @Override
    public void run() {
        try{
            socket.shutdownInput();
            DataInputStream in = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            boolean isOpen = true;

            this.contactList.getContacts(in);            

            socket.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class BackupListener implements Runnable{
    private ServerSocket socket;
    private ContactList contactList;

    public BackupListener(ServerSocket socket, ContactList contactList){
        this.socket = socket;
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

public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ServerSocket backupServerSocket = new ServerSocket(23456);
        ContactList contactList = new ContactList();

        Thread clientListener = new Thread(new ClientListener(serverSocket, contactList));
        Thread backupListener = new Thread(new BackupListener(serverSocket, contactList));

        clientListener.start();
        backupListener.start();

        clientListener.join();
        backupListener.join();
    }

}
