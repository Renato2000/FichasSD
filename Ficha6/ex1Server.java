import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ex1Server {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);

            while (true) {
                Socket socket = ss.accept(); //so aceitamos um novo cliente, quando terminar a conexão com o anterior

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

                out.println((float) sum/n);
                out.flush();

                socket.shutdownOutput(); //são redundantes, porque o close já os fecha automaticamente
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
