/* Import all necessary librarires for the connection 
between client and server and for reading messages 
from the server and writing messages to the server */ 
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class MyClient {
    /* Declare and initialise the strings (messages) that
    will send to the server */
    public static String HELO = "HELO";
    public static String AUTH = "AUTH " + System.getProperty("user.name");
    public static String REDY = "REDY";
    public static String OK = "OK";
    public static String GETSALL = "GETS All";
    public static String QUIT = "QUIT";

    // Constructor for MyClient
    public MyClient() {     }

    // Function for reading messages from the server
    public String readMsg(byte[] b, BufferedInputStream bis) {
        try {
            bis.read(b);
            String str = new String(b, StandardCharsets.UTF_8);
            return str;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "error";
    }

    public static void main (String args[]) throws Exception {
        try {
            /* Open the socket for connection at default
            localhost 127.0.0.1 and port 50000 */
            Socket s = new Socket("localhost", 50000);
            /* Input and ouput stream for reading and 
            writing messages from and to server */
            DataInputStream din =  new DataInputStream(s.getInputStream());
            DataOutputStream dout =  new DataOutputStream(s.getOutputStream());
            BufferedInputStream bin = new BufferedInputStream(din);
            BufferedOutputStream bout = new BufferedOutputStream(dout);

            /* Print message to check the connection
            with the server */
            System.out.println("Connected with the server");

            /* Declare and initialise an instance of 
            MyClient for the communication with the server */
            MyClient mc = new MyClient();

            /* Boolean value to check if there are any jobs
            left that need to be scheduled */
            Boolean jobsLeft = true;

            // String for largest server name
            String largestServer = null;

            /* Firstly, send HELO message to the server to
            start the communication */
            bout.write(HELO.getBytes());
            System.out.println("Sent HELO to the server");
            bout.flush();

            /* Read message from the serevr after the 'HELO'
            message sent (it's supposed to be "OK" from the server) */
            String serverReply = mc.readMsg(new byte[2], bin);
            System.out.println("Received in response to HELO: " + serverReply);

            /* Authorize the user by sending message to the server */
            bout.write(AUTH.getBytes());
            bout.flush();

            /* Read message from server after authorization 
            (it's supposed to be "OK" from the server) */
            serverReply = mc.readMsg(new byte[2], bin);
            System.out.println("Received in response to AUTH: " + serverReply);

            /* Send "REDY" message to the server to say that
            the client is ready to schedule jobs (if any) from the server */
            bout.write(REDY.getBytes());
            bout.flush();

            // While loop for scheduling jobs
            while(jobsLeft) {
                // Get job (if any) from the server
                serverReply = mc.readMsg(new byte[64], bin);
                System.out.println("Received in response to REDY: " + serverReply);

                /* If the message received from the server
                is instead "NONE" or "QUIT", which means
                there are no jobs left to be scheduled, 
                client stop the communication by sending 
                "QUIT" message to the server and quit the
                scheduling */
                if(serverReply.substring(0,4).equals("NONE") || serverReply.substring(0,4).equals(QUIT)) {
                    jobsLeft = false;
                    bout.write(QUIT.getBytes());
                    bout.flush();
                    break;
                }

                /* If it's not actually a Job with the
                specified data, then just get ready for the
                next one by saying "REDY" to the server 
                and keep on scheduling the next one (receiving next message) from the server */
                if(!(serverReply.substring(0,4).equals("JOBN"))) {
                    bout.write(REDY.getBytes());
                    bout.flush();
                    continue;
                }

                /* Else if it's a job with sepcified data 
                that needs to be scheduled, then get job info */
                else {
                    /* Message for job is actually an array of
                    strings separated by spaces */
                    String[] JOBNSplit = serverReply.split("\\s+");
                    // Get job id (it's at index 2 of the array of strings)
                    int JobID = Integer.parseInt(JOBNSplit[2]);

                    // Get all jobs data (length, size)
                    bout.write(GETSALL.getBytes());
                    bout.flush();

                    // Get the reply of jobs data from server
                    serverReply = mc.readMsg(new byte[32], bin);
                    System.out.println("Received in response to GETS All: " + serverReply);

                    // Tells server "OK"
                    bout.write(OK.getBytes());
                    bout.flush();

                    /* The reply is actually an array of 
                    strings separated by space */
                    String[] message_space = serverReply.split(" ");

                    /* String to store the data of the string
                    at the last index in array message_space
                    to remove all non-printable characters */
                    String str = new String();

                    /* Trim (Remove) all non-printable
                    characters at the end of the array of
                    strings message_space (at the end of
                    string at index 2) by checking if 
                    it's actually a number. If it is, 
                    append it to the initialized string str 
                    above to get the recLen of data. Otherwise, not and break the loop */
                    for(int i = 0; i < message_space[2].length(); i++) {
                        char c = message_space[2].charAt(i);
                        if(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' ||c == '6' || c == '7' || c == '8' || c == '9')
                            str += c;
                        else
                            break;
                    }
                    
                    /* Convert string to integer for storing
                    bytes to read message from the server */
                    serverReply = mc.readMsg(new byte[Integer.parseInt(message_space[1])*Integer.parseInt(str)], bin);
                    
                    //System.out.println(serverReply);

                    /* An array of strings that stores a list
                    of servers with information like cores, 
                    server names, bootup times, etc. */
                    String[] arrOfStr = serverReply.split("\n");
                    if(largestServer == null){
                        /* Firstly, randomly, get the first
                        server as the biggest one */
                        String biggestServer = arrOfStr[0];

                        /* Process through each server to 
                        find one with the highest core */ 
                        for(int i = 0; i < arrOfStr.length; i++) {
                            // Current processed server
                            String[] ServerSplitInfo = arrOfStr[i].split("\\s+");
                            /* Current biggest server among 
                            the processed ones */
                            String[] BigSplitInfo = biggestServer.split("\\s+");
                            /* The core number is placed at
                            string index 4 */
                            int currentCore = Integer.parseInt(ServerSplitInfo[4]);
                            int highestCore = Integer.parseInt(BigSplitInfo[4]);
                            /* If current processed core is bigger than the current highest one, get the biggest server as the one with that higher core */
                            if(currentCore > highestCore) {
                                biggestServer = arrOfStr[i];
                            }
                        }
                        // Get the name of the biggest server
                        String[] bigSplit = biggestServer.split("\\s+");
                        largestServer = bigSplit[0];
                    }
                    /* Tells server it's "OK" to schedule the job */
                    bout.write(OK.getBytes());
                    bout.flush();

                    /* Get a response to "OK" as a dot from
                    the server */
                    serverReply = mc.readMsg(new  byte[1], bin);
                    System.out.println("Received in response to OK is a dot: " + serverReply);

                    /* Schedule the job with id JobID to the server id 0 of the largest server */
                    String SCHD = "SCHD" + " " + JobID + " " + largestServer + " " + "0";
                    bout.write(SCHD.getBytes());
                    bout.flush();

                    // Print out the biggest server name
                    System.out.println("The biggest server is: " + largestServer);

                    /* Get the response to the scheduling 
                    decision from the server as "OK" */
                    serverReply = mc.readMsg(new byte[2], bin);
                    System.out.println("Received in response to SCHD: " + serverReply);
                    
                    /* Tells the server it's "REDY" for the 
                    next job, and then gets started over from
                    the beginning of the loop */
                    bout.write(REDY.getBytes());
                    bout.flush();
                }
            }
            /* After the loop terminates (there are no jobs 
            left), QUIT and then get the response to 
            "QUIT" from the server */
            serverReply = mc.readMsg(new byte[32], bin);
            System.out.println("Received in response to QUIT: " + serverReply);

            /* If server replies back as "QUIT", then close 
            all the connections, streams and socket */
            if(serverReply.equals(QUIT)) {
                bout.close();
                dout.close();
                bin.close();
                din.close();
                s.close();
            }
        }
        /* Catch and print on the console any exception
        encountered during the excecution of the program */
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
