package pl.apirog.packet;
/**
 * Class containing a set of static methods for various encryption techniques
 * @author Adam Pirog
 */
public class Encryptor
{

    public static String encodeCezar(String message, int key)
    {
        StringBuilder result = new StringBuilder();

        for (char character : message.toCharArray())
        {
            if(character >= 'a' && character <= 'z')
            {
                int newPosition = (character + key - 'a') % 26;
                if(newPosition < 0)
                {
                    newPosition += 26;
                }
                char newCharacter = (char) ('a' + newPosition);

                result.append(newCharacter);

            }else if (character >= 'A' && character <= 'Z')
            {
                int newPosition = (character + key - 'A') % 26;
                if(newPosition < 0)
                {
                    newPosition += 26;
                }
                char newCharacter = (char) ('A' + newPosition);

                result.append(newCharacter);
            } else
            {
                result.append(character);
            }
        }

        return result.toString();
    }

    public static String decodeCezar(String message, int key)
    {
        return encodeCezar(message, 26 - (key % 26));
    }

    public static String XOR(String message, int key)
    {
        // get the lowest byte of key
        byte byteKey = (byte)(key & 0xFF);
        StringBuilder result = new StringBuilder();

        for (char character : message.toCharArray())
        {
            char newCharacter = (char) (character ^ byteKey);
            result.append(newCharacter);
        }

        return result.toString();
    }
}
