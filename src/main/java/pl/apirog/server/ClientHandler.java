package pl.apirog.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.apirog.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Thread-class handling clients
 * @author Adam Pirog
 */
class ClientHandler implements Runnable
{
    private final String username;
    final DataInputStream inputStream;
    final DataOutputStream outputStream;
    Socket socket;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket, String username, DataInputStream inputStream, DataOutputStream outputStream)
    {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.username = username;
        this.socket = socket;
    }

    /**
     * Main function of the thread. It is responsible for forwarding messages to proper recipients.
     * It also handles disconnecting clients.
     */
    @Override
    public void run()
    {

        String receivedRaw;
        while (!this.socket.isClosed())
        {
            try
            {
                // client shutdown, end of communication
                try{
                    receivedRaw = inputStream.readUTF();
                }catch (EOFException e)
                {
                    System.out.println("Client disconnected: " + username);
                    Server.clients.remove(username);
                    Packet packet = new Packet(username, "All", "disconnected", username);
                    String json = objectMapper.writeValueAsString(packet);

                    for (ClientHandler client : Server.clients.values())
                    {
                        client.outputStream.writeUTF(json);
                    }
                    break;
                }


                System.out.println(receivedRaw);

                Packet receivedDecoded = objectMapper.readValue(receivedRaw, Packet.class);

                ClientHandler client = Server.clients.get(receivedDecoded.getReceiver());

                if (client != null)
                {
                    client.outputStream.writeUTF(receivedRaw);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        try
        {
            socket.close();
            inputStream.close();
            outputStream.close();

        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
