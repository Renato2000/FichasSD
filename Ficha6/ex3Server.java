import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class ServerWorker implements Runnable{
    private Socket socket;

    public ServerWorker(Socket socket){
        this.socket = socket;
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

public class ex3Server {

    public static void main(String[] args) {
        int i = 0;
        try {
            ServerSocket ss = new ServerSocket(12345);

            while (true) {
                Socket socket = ss.accept(); //so aceitamos um novo cliente, quando terminar a conexão com o anterior
                Thread serverWorker = new Thread(new ServerWorker(socket));
                serverWorker.setName("client-" + i++);
                serverWorker.run();         
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
