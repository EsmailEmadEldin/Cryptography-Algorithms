import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Random;

public class Bob {
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

    // Modular inverse using the Extended Euclidean Algorithm
    public static long modInverse(long a, long m) {
        long m0 = m, t, q;
        long x0 = 0, x1 = 1;
        if (m == 1) return 0;
        while (a > 1) {
            q  = a / m;
            t  = m;
            m  = a % m;
            a  = t;
            t  = x0;
            x0 = x1 - q * x0;
            x1 = t;
        }
        if (x1 < 0) x1 += m0;
        return x1;
    }

    // Primality test using trial division with 6k±1 optimisation
    private static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6)
            if (n % i == 0 || n % (i + 2) == 0) return false;
        return true;
    }

    // Check that alpha is a primitive root modulo p:
    // verify alpha^(p-1) ≡ 1 mod p and no smaller exponent gives 1
    private static boolean isPrimitiveRoot(int alpha, int p) {
        if (alpha <= 1 || alpha >= p) return false;
        if (modPow(alpha, p - 1, p) != 1) return false;
        long pow = 1;
        for (int i = 1; i < p - 1; i++) {
            pow = (pow * alpha) % p;
            if (pow == 1) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== ElGamal Encryption Setup ===");

        // --- Step 1: Validate prime p ---
        int p = 0;
        while (true) {
            System.out.print("Enter prime number p: ");
            p = scanner.nextInt();
            if (!isPrime(p)) {
                System.out.println("Invalid! " + p + " is not a prime number. Please try again.");
                continue;
            }
            if (p < 2) {
                System.out.println("Invalid! p must be at least 2. Please try again.");
                continue;
            }
            break;
        }

        // --- Step 2: Validate primitive root alpha ---
        int alpha = 0;
        while (true) {
            System.out.print("Enter primitive root α (2 to " + (p - 1) + "): ");
            alpha = scanner.nextInt();
            if (alpha < 2 || alpha >= p) {
                System.out.println("Invalid! α must be between 2 and " + (p - 1) + ". Please try again.");
                continue;
            }
            if (!isPrimitiveRoot(alpha, p)) {
                System.out.println("Invalid! " + alpha + " is not a primitive root modulo " + p + ". Please try again.");
                continue;
            }
            break;
        }

        // --- Step 3: Validate private key d ---
        int d = 0;
        while (true) {
            System.out.print("Enter private key d (2 to " + (p - 2) + "): ");
            d = scanner.nextInt();
            if (d < 2 || d > p - 2) {
                System.out.println("Invalid! d must be between 2 and " + (p - 2) + ". Please try again.");
                continue;
            }
            break;
        }

        // --- Step 4: Compute public key beta = α^d mod p ---
        long beta = modPow(alpha, d, p);

        System.out.println("\n=== Valid Parameters ===");
        System.out.println("p = " + p     + " (prime)");
        System.out.println("α = " + alpha + " (primitive root)");
        System.out.println("d = " + d     + " (private key)");
        System.out.println("β = α^d mod p = " + beta);

        // --- Step 5: Open server socket, wait for Alice ---
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("\nBob is listening on port 12345...");
            Socket socket = serverSocket.accept();
            System.out.println("Alice connected.");

            DataInputStream  in  = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Send public parameters to Alice
            out.writeUTF(Integer.toString(p));
            out.writeUTF(Integer.toString(alpha));
            out.writeUTF(Long.toString(beta));
            out.flush();

            System.out.println("\nPublic parameters sent to Alice:");
            System.out.println("  p = " + p);
            System.out.println("  α = " + alpha);
            System.out.println("  β = " + beta);

            // --- Step 6: Receive ciphertext (k_E, y) from Alice ---
            int kE = Integer.parseInt(in.readUTF());
            int y  = Integer.parseInt(in.readUTF());

            // --- Step 7: Decrypt ---
            // k_M = k_E^d mod p
            // x   = y × k_M⁻¹ mod p
            long kM    = modPow(kE, d, p);
            long kM_inv = modInverse(kM, p);
            long x     = (y * kM_inv) % p;

            System.out.println("\nReceived ciphertext: (k_E = " + kE + ", y = " + y + ")");
            System.out.println("Masking key:         k_M = " + kM);
            System.out.println("Decrypted message:   x   = " + x);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
