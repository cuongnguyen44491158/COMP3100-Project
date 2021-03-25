import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class myClient {

    public static void main(String[] args){
        try {
            Socket s = new Socket ("localhost", 50000);
            InputStream din = new DataInputStream(s.getInputStream());
            OutputStream dout = new DataOutputStream(s.getOutputStream());
            String myMessage = "HELO";

            dout.write(myMessage.getBytes());
            dout.flush();
            System.out.println("Client sent \""
            + myMessage + "\" to the server");

            byte[] byteArray = new byte[din.available()];
            din.read(byteArray);
            myMessage = new String(byteArray, StandardCharsets.UTF_8);
            System.out.println(myMessage + " is received from the server");

            myMessage = "AUTH huucuong";
            byteArray = myMessage.getBytes();
            dout.write(byteArray);
            dout.flush();

            byte[] byteArray1 = new byte[2];
            din.read(byteArray1);
            myMessage = new String(byteArray1, StandardCharsets.UTF_8);
            System.out.println("Another OK from server: " + myMessage);
            
            myMessage = "REDY";
            byteArray = myMessage.getBytes();
            dout.write(byteArray);
            dout.flush();

            byte[] byteArray2 = new byte[5];
            din.read(byteArray2);
            myMessage = new String(byteArray2, StandardCharsets.UTF_8);
            System.out.println("Server says: " + myMessage);
            
            myMessage = "GETS  ALL";
            byteArray = myMessage.getBytes();
            dout.write(byteArray);
            dout.flush();

            byte[] byteArray3 = new byte[124*124];
            din.read(byteArray3);
            myMessage = new String(byteArray3, StandardCharsets.UTF_8);
            System.out.println("Receives all replies: " + myMessage);

            myMessage = "OK";
            dout.write(myMessage.getBytes());
            dout.flush();

            byte[] byteArray4 = new byte[184*124];
            din.read(byteArray4);
            myMessage = new String(byteArray4, StandardCharsets.UTF_8);
            System.out.println("Receives all replies: " + myMessage);

            myMessage = "OK";
            dout.write(myMessage.getBytes());
            dout.flush();

            if(myMessage == "" || myMessage == "NONE") {
                myMessage = "QUIT";
                byteArray = myMessage.getBytes();
                dout.write(byteArray);
                dout.flush();
            }

            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}