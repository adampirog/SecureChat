package pl.apirog.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.apirog.packet.Packet;
import pl.apirog.packet.Secret;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Client class
 * @author Adam Pirog
 */
public class Client
{
    final static int SERVER_PORT = 9999;
    final static String SERVER_ADDRESS = "127.0.0.1";


    private final String username;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private final Map<String, Secret> secretKeys = Collections.synchronizedMap(new HashMap<>());

    public Client(String username, Socket socket)
    {
        this.socket = socket;
        this.username = username;

        try{
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());

            //Register the username with the server
            outputStream.writeUTF(username);
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public Map<String, Secret> getSecretKeys() {
        return secretKeys;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectMapper getObjectMapper() {return objectMapper;}

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public String getUsername(){
        return username;
    }

    public void sendPacket(Packet packet) throws IOException
    {
        String json = objectMapper.writeValueAsString(packet);
        synchronized (socket)
        {
            outputStream.writeUTF(json);
        }

    }

    public static void safePrint(String message)
    {
        synchronized (System.out)
        {
            System.out.print(message);
        }
    }

    /**
     * Main method responsible for creating a connection with server and registering username
     * Creates reader and writer thread for further communication
     */
    public static void main(String[] args)
    {

        Scanner scanner = new Scanner(System.in);
        Socket socket = null;

        //initiate server connection
        try{
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        }catch (IOException e)
        {
            safePrint("Server connection error. \n");
            System.exit(1);
        }

        // initiate client
        safePrint("Enter your username: ");
        String username = scanner.nextLine();

        while (username == null || username.trim().equals(""))
        {
            safePrint("Invalid username \n");
            safePrint("Enter your username: ");
            username = scanner.nextLine();
        }

        Client client = new Client(username.trim(), socket);


        // start client threads
        Thread sendMessage = new Thread(new WriterThread(client));
        Thread readMessage = new Thread(new ReaderThread(client));

        safePrint("Connection successful. \n");

        readMessage.start();
        sendMessage.start();

    }
}
