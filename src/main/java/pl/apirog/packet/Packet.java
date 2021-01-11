package pl.apirog.packet;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing the packet send between clients
 * It is designed to work with Jackson library - it can be marshalled into JSON format
 * @author Adam Pirog
 */
public class Packet
{

    @JsonProperty("From")
    private String sender;
    @JsonProperty("To")
    private String receiver;

    @JsonProperty("Type")
    private String type;
    @JsonProperty("Body")
    private String body;

    public Packet(String from, String to, String type, String body)
    {
        this.sender = from;
        this.receiver = to;

        this.type = type;
        this.body = body;

    }

    public Packet(){}

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getType() {
        return type;
    }


}
