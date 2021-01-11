package pl.apirog.packet;

import java.util.Random;

/**
* Class representing a secret used to encrypt a message.
* It is designed to support Diffie–Hellman key exchange protocol.
* Holds the key itself, it's components and methods for the calculation.
*
* @author Adam Pirog
 */
public class Secret
{
    private Integer key = null;
    private String encryptionMethod = "none";

    private final int prime;
    private final int base;

    private int myPrivateElement, myPublicElement, otherPublicElement;
    private volatile boolean recipientReady = false;


    public Secret()
    {
        Random rand = new Random();
        this.prime = 1013;
        this.base = 2 + rand.nextInt(1000);
    }

    public Secret(int prime, int base)
    {
        this.prime = prime;
        this.base = base;
    }

    public void setReady()
    {
        synchronized (this)
        {
            recipientReady = true;
            this.notifyAll();
        }

    }

    public String getBase()
    {
        return prime + "," + base;
    }

    public void setMyPrivateElement()
    {
        Random rand = new Random();
        this.myPrivateElement = 2 + rand.nextInt(1000);
        this.calculateMyPublicElement();
    }

    public int getMyPublicElement() {
        return myPublicElement;
    }

    private void calculateMyPublicElement()
    {
        this.myPublicElement = modPow(base, myPrivateElement, prime);
    }

    public void setOtherPublicElement(int otherPublicElement) {
        this.otherPublicElement = otherPublicElement;
    }

    public Integer getKey()
    {
        int timeoutSeconds = 5;

        synchronized (this)
        {
            try {
                long timeoutExpiredMs = System.currentTimeMillis() + (timeoutSeconds * 1000);
                long waitMillis;

                while (!recipientReady)
                {
                    waitMillis = timeoutExpiredMs - System.currentTimeMillis();
                    if (waitMillis <= 0) break;
                    this.wait(waitMillis);
                }

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if(!recipientReady) return null;
        else return key;

    }


    public void calculateKey()
    {
        this.key = modPow(otherPublicElement, myPrivateElement, prime);
    }

    public String getEncryptionMethod() {
        return encryptionMethod;
    }

    public void setEncryptionMethod(String encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    /**
     * Efficient method for quick modulo power calculation based on bit shifting
     * @param base of the modulo power operation
     * @param exponent of the modulo power operation
     * @param modulo of the modulo power operation
     * @return modulo power
     */
    private static int modPow(int base, int exponent, int modulo)
    {
        int result = 1;
        base = base % modulo;

        if (base == 0) return 0;

        while (exponent > 0)
        {
            if((exponent & 1) == 1)
                result = (result * base) % modulo;

            exponent = exponent >> 1;
            base = (base * base) % modulo;
        }
        return result;
    }
}
