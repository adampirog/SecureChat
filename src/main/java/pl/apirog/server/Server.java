package pl.apirog.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Server class
 * Listens on a given port and accepts new clients via socket,
 * then creates client-handling threads for each client.
 * @author Adam Pirog
 */
public class Server
{
    final static int SERVER_PORT = 9999;
    static Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        Socket socket;

        System.out.println("Server running");

        while (true)
        {
            socket = serverSocket.accept();

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            String username = inputStream.readUTF();
            System.out.println("New client: " + username);

            ClientHandler client = new ClientHandler(socket, username, inputStream, outputStream);

            Thread thread = new Thread(client);

            clients.put(username, client);
            thread.start();

        }
    }
}
