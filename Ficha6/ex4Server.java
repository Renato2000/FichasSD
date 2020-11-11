import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Register{
    private ReentrantLock lock = new ReentrantLock();
    private int sum;
    private int n = 0;

    public void sum(int value){
        lock.lock();
        try{
            sum += value;
            n++;
        } finally {
            lock.unlock();
        }
    }

    public float avg(){
        lock.lock();
        try{
            if(n < 1){
                return 0;

            }
            
            return (float) sum/n;
        }
        finally{
            lock.unlock();
        }
    }
}

class ServerWorker2 implements Runnable{
    private Socket socket;
    private final Register register;

    public ServerWorker2(Socket socket, Register register){
        this.socket = socket;
        this.register = register;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                String line;
                int sum = 0;
                int n = 0;
                while ((line = in.readLine()) != null) {
                    try{
                        int number = Integer.parseInt(line); // @TODO: handle invlaid integers ...
                        sum += number;
                        n++;
                    } catch (NumberFormatException e) {
                        out.println("error");
                        out.flush();
                        continue;
                    }

                    out.println(sum);
                    out.flush();
                }

                if(n < 1){
                    n = 1;
                }

                out.println((float) sum/n);
                out.flush();

                socket.shutdownOutput(); //são redundantes, porque o close já os fecha automaticamente
                socket.shutdownInput();
                socket.close();
        }
        catch(Exception e) {

        }
    }
}

public class ex4Server {

    public static void main(String[] args) {
        int i = 0;
        try {
            ServerSocket ss = new ServerSocket(12345);
            Register register = new Register();

            while (true) {
                Socket socket = ss.accept(); //so aceitamos um novo cliente, quando terminar a conexão com o anterior
                Thread serverWorker = new Thread(new ServerWorker2(socket, register));
                serverWorker.setName("client-" + i++);
                serverWorker.start();         
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
