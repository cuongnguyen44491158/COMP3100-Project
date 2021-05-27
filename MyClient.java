/* Import all necessary librarires for the connection 
between client and server and for reading messages 
from the server and writing messages to the server */ 
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MyClient {
    /* Declare and initialise the address, port number, and strings (messages with new line chracters) that
    will send to the server (handling in -n option) */
    public static String ADDRESS = "localhost";
    public static Integer PORT = 50000;
    public static String HELO = "HELO" + "\n";
    public static String AUTH = "AUTH " + System.getProperty("user.name") + "\n";
    public static String REDY = "REDY" + "\n";
    public static String OK = "OK" + "\n";
    public static String GETSCAP = "GETS Capable";
    public static String QUIT = "QUIT" + "\n";

    // Constructor for MyClient
    public MyClient() {     }

    // Function for initiating communication with the server
    public static void handshake(BufferedOutputStream bout, BufferedReader br) {
        try {
            /* Firstly, send HELO message to the server to
            start the communication */
            bout.write(HELO.getBytes());
            bout.flush();
                
            /* Read message from the server after the 'HELO'
            message sent (it's supposed to be "OK" from the server) */
            String serverReply = br.readLine();
        
            /* Authorize the user by sending message to the server */
            bout.write(AUTH.getBytes());
            bout.flush();

            /* Read message from server after authorization 
            (it's supposed to be "OK" from the server) */
            serverReply = br.readLine();

            /* Send "REDY" message to the server to say that
            the client is ready to schedule jobs (if any) from the server */
            bout.write(REDY.getBytes());
            bout.flush();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    // Function for getting a list of job info (like id, core, memory, disk) as integers
    public static List<Integer> parseJOBN (String serverReply) {
        List<Integer> info = new ArrayList<Integer>();
        /* Message for job is actually an array of
        strings separated by spaces */
        String[] JOBNSplit = serverReply.split("\\s+");
        // Get job id, job cores, job memory and job disk 
        int JobID = Integer.parseInt(JOBNSplit[2]);
        int JobCores = Integer.parseInt(JOBNSplit[4]);
        int JobMemory = Integer.parseInt(JOBNSplit[5]);
        int JobDisk = Integer.parseInt(JOBNSplit[6]);
        info.add(JobID);
        info.add(JobCores);
        info.add(JobMemory);
        info.add(JobDisk);
        return info;
    }

    // Function for client requesting "GETS Capable" and receiving response from the server side as a list of string containing number of records and record length data
    public static String[] handleRequest (BufferedOutputStream bout, BufferedReader br, int JobCores, int JobMemory, int JobDisk) {
        try {
            // Get all server state information (servers that are capable of running the jobs)
            bout.write((GETSCAP + " " + JobCores + " " + JobMemory + " " + JobDisk + "\n").getBytes());
            bout.flush();

            // Get the reply of number of capable servers from server
            String serverReply = br.readLine();

            // Tells server "OK"
            bout.write(OK.getBytes());
            bout.flush();

            /* The reply is actually an array of 
            strings separated by space */
            String[] message_space = serverReply.split(" ");
            return message_space;
        }
        catch (Exception e) {
            return null;
        }
    }

    // Function for getting the list of servers responded back by the server side
    public static String[] serverList (BufferedReader br, String[] message_space) {
        try{
            // Get number of records (servers) that are capable of running the current job
            int numRec = Integer.parseInt(message_space[1]);

            // Store servers info in an array of string
            /* An array of strings that stores a list
            of servers with information like cores, 
            server names, bootup times, etc. */
            String[] servers = new String[numRec];
            for(int i = 0; i < numRec; i++)
                servers[i] = br.readLine();
            return servers;
        }
        catch (Exception e) {
            return null;
        }
    }

    // Function for getting smallest server among the list of servers
    public static String getSmallestServer(String[] servers) {
        // Initally, randomly get the first server as the smallest one
        String smallestServer = servers[0];

        for(int i = 0; i < servers.length; i++) {
            // Current processed server
            String[] ServerSplitInfo = servers[i].split("\\s+");
            /* Current smallest server among 
            the processed ones */
            String[] SmallSplitInfo = smallestServer.split("\\s+");
            /* The core number is placed at
            string index 4 */
            int currentCore = Integer.parseInt(ServerSplitInfo[4]);
            int smallestCore = Integer.parseInt(SmallSplitInfo[4]);

            /* If current processed core is smaller than the current smallest one, 
            get the smallest server as the one with that smaller core */
            if(currentCore < smallestCore)                       
                smallestServer = servers[i];
        }
        return smallestServer;
    }

    // Function for scheduling the job with specific passed information
    public static void scheduleJob(BufferedOutputStream bout, BufferedReader br, int JobID, String serverName, String serverID) {
        try {
            /* Schedule the job with id JobID to the smallest server with its name and id */
            String SCHD = "SCHD" + " " + JobID + " " + serverName + " " + serverID + "\n";
            bout.write(SCHD.getBytes());
            bout.flush();
            
            /* Get the response to the scheduling 
            decision from the server as "OK" */
            String serverReply = br.readLine();
            
            /* Tells the server it's "REDY" for the 
            next job, and then gets started over from
            the beginning of the loop */
            bout.write(REDY.getBytes());
            bout.flush();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void main (String args[]) throws Exception {
        try {
            /* Open the socket for connection at default
            localhost 127.0.0.1 and port 50000 */
            Socket s = new Socket(ADDRESS, PORT);
            /* Input and ouput stream for reading and 
            writing messages from and to server */
            DataOutputStream dout =  new DataOutputStream(s.getOutputStream());
            BufferedOutputStream bout = new BufferedOutputStream(dout);
            InputStreamReader r = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(r);

            String serverReply;
            /* Boolean value to check if there are any jobs
            left that need to be scheduled */
            Boolean jobsLeft = true;

            // Client performs handshake with the server for initiating communication between them
            handshake(bout, br);
            
            // While loop for scheduling jobs
            while(jobsLeft) {
                // Get job (if any) from the server
                serverReply = br.readLine();

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
                    // Get a list of job data (represented as integer) (like submit time, core, memory, disk, etc.)
                    List<Integer> jobInfo = parseJOBN(serverReply);

                    // Get the response (as data containing number of records and record length) from the server for the request "GETS Capable" from the clients
                    String[] message_space = handleRequest(bout, br, jobInfo.get(1), jobInfo.get(2), jobInfo.get(3));
                    
                    // Get a list of capable servers
                    String[] servers = serverList(br, message_space);
                    
                    // Get the smallest server among the list
                    String smallestServer = getSmallestServer(servers);
                    
                    // Get the name and id of smallest server
                    String[] smallSplit= smallestServer.split("\\s+");
                    // Strings for smallest server name and ID
                    String smallestServerName = smallSplit[0];
                    String smallestServerID = smallSplit[1];

                    /* Tells server it's "OK" to schedule the job */
                    bout.write(OK.getBytes());
                    bout.flush();

                    /* Get a response to "OK" as a dot from
                    the server */
                    serverReply = br.readLine();

                    // Schedule the job with specific job id and server name and server id
                    scheduleJob(bout, br, jobInfo.get(0), smallestServerName, smallestServerID);
                }
            }
             /* After the loop terminates (there are no jobs 
            left), QUIT and then get the response to 
            "QUIT" from the server */
            serverReply = br.readLine();
            //System.out.println("Received in response to QUIT: " + serverReply);

            /* If server replies back as "QUIT", then close 
            all the connections, streams and socket */
            if(serverReply.equals(QUIT)) {
                bout.close();
                dout.close();
                r.close();
                br.close();
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