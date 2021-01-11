package pl.apirog.client;

import pl.apirog.packet.Encryptor;
import pl.apirog.packet.Packet;
import pl.apirog.packet.Secret;

import java.io.IOException;
import java.net.SocketException;
import java.util.Base64;
import java.util.Scanner;

/**
 * Thread-class responsible for sending the messages
 * It also initiates the key exchange handshake sequence
 * @author Adam Pirog
 */
public class WriterThread implements Runnable
{
    private final Client client;
    private final Scanner scanner = new Scanner(System.in);

    public WriterThread(Client client)
    {
        this.client = client;
    }

    private void initiateHandshake(String recipient)
    {
        this.client.getSecretKeys().put(recipient, new Secret());
        String body  = this.client.getSecretKeys().get(recipient).getBase();

        Packet packet = new Packet(this.client.getUsername(), recipient, "handshake0", body);
        //sending
        try {
            client.sendPacket(packet);

        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    //Handles encryption and base64 encoding
    private Packet writeMessage(String recipient, String message)
    {

        Secret secret = client.getSecretKeys().get(recipient);
        if(secret == null)
        {
            initiateHandshake(recipient);
        }
        secret = client.getSecretKeys().get(recipient);
        Integer key = secret.getKey();

        if (key == null)
        {
            Client.safePrint("Error. Invalid recipient.  \n");
            return null;
        }
        String encrypted;

        if(secret.getEncryptionMethod().equals("xor"))
        {
            encrypted = Encryptor.XOR(message, key);
        }else if(secret.getEncryptionMethod().equals("caesar"))
        {
            encrypted = Encryptor.encodeCezar(message, key);
        }else
        {
            encrypted = message;
        }


        String encoded64 = Base64.getEncoder().encodeToString(encrypted.getBytes());


        return new Packet(client.getUsername(), recipient,"message", encoded64);
    }

    private void changeEncryptionMethod(String recipient, String type)
    {
        Packet packet = new Packet(client.getUsername(), recipient, "encryption", type);
        client.getSecretKeys().get(recipient).setEncryptionMethod(type);
        try {
            client.sendPacket(packet);
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        String recipient, command;
        while (!client.getSocket().isClosed())
        {
            command = scanner.nextLine();

            if(command.equals("!logout"))
            {
                try
                {
                    client.getSocket().close();
                    break;
                }catch (IOException e)
                {
                    e.printStackTrace();
                    break;
                }

            }else if (command.contains("!to"))
            {
                recipient = command.replace("!to", " ").trim();
                if(recipient.equals(this.client.getUsername()))
                {
                    Client.safePrint("Error. Invalid recipient. \n");
                    continue;
                }

                String message = scanner.nextLine();
                if (message.contains("!encryption"))
                {
                    String method = message.replace("!encryption", " ").trim();

                    if(method.equals("xor") || method.equals("caesar") || method.equals("none"))
                    {
                        changeEncryptionMethod(recipient, method);
                    }else
                    {
                        Client.safePrint("Error. Unsupported encryption method  \n");
                        continue;
                    }

                    message = scanner.nextLine();
                }
                Packet packet = writeMessage(recipient, message);

                if(packet == null) continue;


                //sending the message
                try {
                    client.sendPacket(packet);

                } catch (SocketException e)
                {
                    Client.safePrint("Thread closing  \n");
                    break;
                }catch (IOException ex)
                {
                    ex.printStackTrace();
                    break;
                }

            }else
            {
                Client.safePrint("Unknown command.  \n");
            }



        }
    }
}
