import java.util.Arrays;
import java.util.Scanner;

class LFSR {
    private String initialseed;  // current state of the LFSR (binary string)
    private int[] tapposition;   // tap positions for the feedback
    private int numtapposition;  // number of taps

    public LFSR(String seed, int[] tap, int n) {
        this.initialseed = seed;
        this.numtapposition = n;
        this.tapposition = Arrays.copyOf(tap, n);
    }

    /**
     * Generates the next bit of the LFSR by calculating the feedback
     * and shifting the register left by one position.
     */
    public int step() {
        int feedback = initialseed.charAt(tapposition[0]) - '0';
        for (int i = 1; i < numtapposition; i++) {
            feedback ^= (initialseed.charAt(tapposition[i]) - '0');
        }
        initialseed = initialseed.substring(1) + feedback;
        return feedback;
    }

    /**
     * Generates a keystream of the specified number of bits.
     * @param bits number of bits to generate
     * @return binary string of generated bits
     */
    public String sequence(int bits) {
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < bits; i++) {
            sequence.append(step());
        }
        return sequence.toString();
    }

    /**
     * Encrypts or decrypts a binary string using XOR with a keystream.
     * @param text      binary string (plaintext or ciphertext)
     * @param keystream binary keystream of equal length
     * @return XOR result as binary string
     */
    public String EncryptandDecrypt(String text, String keystream) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((text.charAt(i) == keystream.charAt(i)) ? '0' : '1');
        }
        return result.toString();
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the initial seed: ");
        String seed = scanner.nextLine();

        System.out.print("Enter number of tap positions: ");
        int numoftaps = scanner.nextInt();

        int[] taps = new int[10];
        System.out.print("Enter tap positions (space-separated): ");
        for (int i = 0; i < numoftaps; i++) {
            taps[i] = scanner.nextInt();
        }

        System.out.print("Enter the number of bits to generate: ");
        int numofbits = scanner.nextInt();

        LFSR F1 = new LFSR(seed, taps, numoftaps);
        String keystream = F1.sequence(numofbits);
        System.out.println("Keystream:      " + keystream);

        scanner.nextLine(); // consume leftover newline
        System.out.print("Enter the plaintext (binary string): ");
        String plaintext = scanner.nextLine();

        String ciphertext = F1.EncryptandDecrypt(plaintext, keystream);
        System.out.println("Ciphertext:     " + ciphertext);

        String decryptedtext = F1.EncryptandDecrypt(ciphertext, keystream);
        System.out.println("Decrypted text: " + decryptedtext);

        scanner.close();
    }
}
