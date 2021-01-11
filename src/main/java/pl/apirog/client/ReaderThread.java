package pl.apirog.client;

import pl.apirog.packet.Encryptor;
import pl.apirog.packet.Packet;
import pl.apirog.packet.Secret;

import java.io.IOException;
import java.net.SocketException;
import java.util.Base64;


/**
 * Thread-class responsible for reading the incoming messages
 * It also handles the key exchange handshake sequence
 * @author Adam Pirog
 */
public class ReaderThread implements Runnable
{
    private final Client client;

    public ReaderThread(Client client)
    {
        this.client = client;
    }

    //Handles decryption and base64 decoding
    private String readMessage(Packet packet)
    {
        String decoded = new String(Base64.getDecoder().decode(packet.getBody()));
        Secret secret = client.getSecretKeys().get(packet.getSender());

        if(secret.getEncryptionMethod().equals("xor"))
        {
            return Encryptor.XOR(decoded, secret.getKey());
        }else if(secret.getEncryptionMethod().equals("caesar"))
        {
            return Encryptor.decodeCezar(decoded, secret.getKey());
        }
        else
            return decoded;

    }

    private void sendHandShake1(String recipient)
    {
        Secret secret = client.getSecretKeys().get(recipient);
        secret.setMyPrivateElement();
        Packet packet = new Packet(this.client.getUsername(), recipient, "handshake1", String.valueOf(secret.getMyPublicElement()));


        try {
            client.sendPacket(packet);
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private void sendHandShake2(String recipient, Secret secret)
    {
        secret.setMyPrivateElement();
        secret.calculateKey();

        Packet packet = new Packet(this.client.getUsername(), recipient, "handshake2", String.valueOf(secret.getMyPublicElement()));

        try
        {
           client.sendPacket(packet);
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private void sendHandShake3(String recipient)
    {
        Packet packet = new Packet(this.client.getUsername(), recipient, "handshake3", "Ready");

        try
        {
            client.sendPacket(packet);
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    @Override
    public void run()
    {
        while (!client.getSocket().isClosed())
        {
            try
            {
                String received = client.getInputStream().readUTF();
                Packet packet = client.getObjectMapper().readValue(received, Packet.class);

                switch (packet.getType()) {
                    case "message": {

                        Client.safePrint("\nFrom: ");
                        Client.safePrint(packet.getSender() + " \n");
                        Client.safePrint(readMessage(packet) + " \n");
                        break;
                    }
                    case "encryption":{

                        this.client.getSecretKeys().get(packet.getSender()).setEncryptionMethod(packet.getBody());
                        break;
                    }
                    case "handshake0": {
                        String[] numbers = packet.getBody().split(",");
                        int p, g;
                        p = Integer.parseInt(numbers[0]);
                        g = Integer.parseInt(numbers[1]);

                        this.client.getSecretKeys().put(packet.getSender(), new Secret(p, g));
                        sendHandShake1(packet.getSender());
                        break;
                    }
                    case "handshake1": {
                        int A = Integer.parseInt(packet.getBody());
                        Secret secret = this.client.getSecretKeys().get(packet.getSender());
                        secret.setOtherPublicElement(A);

                        sendHandShake2(packet.getSender(), secret);
                        break;
                    }
                    case "handshake2": {
                        int B = Integer.parseInt(packet.getBody());

                        Secret secret = this.client.getSecretKeys().get(packet.getSender());
                        secret.setOtherPublicElement(B);
                        secret.calculateKey();
                        secret.setReady();
                        sendHandShake3(packet.getSender());

                        break;
                    }
                    case "handshake3": {
                        Secret secret = this.client.getSecretKeys().get(packet.getSender());
                        secret.setReady();
                    }
                    case "disconnected": {
                        this.client.getSecretKeys().remove(packet.getBody());
                        break;
                    }
                }

            } catch (SocketException e)
            {
                Client.safePrint("Thread closing  \n");
                break;
            }catch (IOException ex)
            {
                ex.printStackTrace();
                break;
            }
        }
    }
}
