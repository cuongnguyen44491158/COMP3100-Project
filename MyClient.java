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
    public static String GETSCAP = "GETS Capable";
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

    /* Trim (Remove) all non-printable
    characters at the end of the array of
    strings s (at the last index) by checking if 
    it's actually a number. If it is, append it to the result string 
    above to get the required data. Otherwise, not and break the loop */
    public String trimNonPrintableCharacters (String s) {
        String result = new String();
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' ||c == '6' || c == '7' || c == '8' || c == '9')
                result += c;
            else
                break;
        }
        return result;
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
            String smallestServerName= null;
            String smallestServerID = null;

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
                    // Get job id, job cores, job memory and job disk 
                    int JobID = Integer.parseInt(JOBNSplit[2]);
                    int JobCores = Integer.parseInt(JOBNSplit[4]);
                    int JobMemory = Integer.parseInt(JOBNSplit[5]);
                    String disk = mc.trimNonPrintableCharacters(JOBNSplit[6]);
                    int JobDisk = Integer.parseInt(disk);

                    // Get all server state information (servers that are capable of running the jobs)
                    bout.write((GETSCAP + " " + JobCores + " " + JobMemory + " " + JobDisk).getBytes());
                    bout.flush();

                    // Get the reply of number of capable servers from server
                    serverReply = mc.readMsg(new byte[32], bin);
                    System.out.println("Received in response to GETS Capable: " + serverReply);

                    // Tells server "OK"
                    bout.write(OK.getBytes());
                    bout.flush();

                    /* The reply is actually an array of 
                    strings separated by space */
                    String[] message_space = serverReply.split(" ");
                    
                    String recLen = mc.trimNonPrintableCharacters(message_space[2]);

                    /* Convert string to integer for storing
                    bytes to read message from the server */
                    serverReply = mc.readMsg(new byte[Integer.parseInt(message_space[1])*Integer.parseInt(recLen)], bin);
                    
                    // System.out.println(serverReply);

                    /* An array of strings that stores a list
                    of servers with information like cores, 
                    server names, bootup times, etc. */
                    String[] arrOfStr = serverReply.split("\n");
                    /* First of all, randomly, get the first
                    server as the smallest one */
                    String smallestServer = arrOfStr[0];
                    String[] smallestSplitInfo = arrOfStr[0].split("\\s+");
                    // Core count of this server
                    int cores = Integer.parseInt(smallestSplitInfo[4]);
                    for(int i = 0; i < arrOfStr.length; i++) {
                        // Current processed server
                        String[] ServerSplitInfo = arrOfStr[i].split("\\s+");
                        /* The number of cores is placed at
                        string index 4 */
                        int currCores = Integer.parseInt(ServerSplitInfo[4]);

                        if(currCores < cores) {
                            cores = currCores;
                            smallestServer = arrOfStr[i];
                        }
                    }
                    // Get the name and id of capable server
                    String[] smallestSplit = smallestServer.split("\\s+");
                    smallestServerName = smallestSplit[0];
                    smallestServerID = smallestSplit[1];

                    /* Tells server it's "OK" to schedule the job */
                    bout.write(OK.getBytes());
                    bout.flush();

                    /* Get a response to "OK" as a dot from
                    the server */
                    serverReply = mc.readMsg(new  byte[1], bin);
                    System.out.println("Received in response to OK is a dot: " + serverReply);

                    /* Schedule the job with id JobID to the smallest server with its name and id */
                    String SCHD = "SCHD" + " " + JobID + " " + smallestServerName + " " + smallestServerID;
                    bout.write(SCHD.getBytes());
                    bout.flush();

                    // Print out the capable server name and id
                    System.out.println("The smallest server is: " + smallestServerName + " " + smallestServerID);

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