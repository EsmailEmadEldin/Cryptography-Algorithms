import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Alice {
    private static final Random random = new Random();

    // Fast modular exponentiation using square-and-multiply
    public static long modPow(long base, long exp, long mod) {
        long result = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1)
                result = (result * base) % mod;
            base = (base * base) % mod;
            exp >>= 1;
        }
        return result;
    }

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("Connected to Bob.");

            DataInputStream  in  = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // --- Step 1: Receive public parameters from Bob ---
            int  p     = Integer.parseInt(in.readUTF());
            int  alpha = Integer.parseInt(in.readUTF());
            long beta  = Long.parseLong(in.readUTF());

            System.out.println("Received from Bob:");
            System.out.println("  p = " + p);
            System.out.println("  α = " + alpha);
            System.out.println("  β = " + beta);

            // --- Step 2: Get plaintext message from user ---
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the message to encrypt (1 to " + (p - 1) + "): ");
            int x = scanner.nextInt();
            while (x < 1 || x >= p) {
                System.out.print("Invalid. Enter a number between 1 and " + (p - 1) + ": ");
                x = scanner.nextInt();
            }
            scanner.close();

            // --- Step 3: Choose a random ephemeral private key i in [2, p-2] ---
            int i = random.nextInt(p - 3) + 2;
            System.out.println("Ephemeral private key: i = " + i);

            // --- Step 4: Compute encryption components ---
            // k_E = α^i mod p  (sent to Bob to let him recompute k_M)
            // k_M = β^i mod p  (masking key — shared secret)
            long kE = modPow(alpha, i, p);
            long kM = modPow(beta,  i, p);

            // --- Step 5: Encrypt: y = x × k_M mod p ---
            long y = (x * kM) % p;

            System.out.println("\nPlaintext message:  x   = " + x);
            System.out.println("Masking key:        k_M = " + kM);
            System.out.println("Ciphertext:         (k_E = " + kE + ", y = " + y + ")");

            // --- Step 6: Send ciphertext to Bob ---
            out.writeUTF(Long.toString(kE));
            out.writeUTF(Long.toString(y));
            out.flush();

            System.out.println("Encrypted message sent to Bob.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
