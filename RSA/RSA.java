import java.util.Scanner;

public class RSA {

    // Check if a number is prime using trial division up to sqrt(n)
    static boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // Compute GCD using the iterative Euclidean algorithm
    static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // Compute modular inverse of e mod phi using the Extended Euclidean Algorithm
    // Returns -1 if no inverse exists (gcd != 1)
    static int modInverse(int e, int phi) {
        int t = 0, newT = 1;
        int r = phi, newR = e;

        while (newR != 0) {
            int quotient = r / newR;

            int tempT = t;
            t = newT;
            newT = tempT - quotient * newT;

            int tempR = r;
            r = newR;
            newR = tempR - quotient * newR;
        }

        if (r > 1) return -1;   // e and phi are not coprime — no inverse
        if (t < 0) t += phi;    // ensure positive result
        return t;
    }

    // Fast modular exponentiation: computes (base^exp) % mod using repeated squaring
    static int modPow(int base, int exp, int mod) {
        int result = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1)
                result = (result * base) % mod;
            base = (base * base) % mod;
            exp >>= 1;
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // --- Step 1: Input and validate primes ---
        System.out.print("Enter first prime number (p): ");
        int p = input.nextInt();

        System.out.print("Enter second prime number (q): ");
        int q = input.nextInt();

        if (!isPrime(p) || !isPrime(q)) {
            System.out.println("Error: Both numbers must be prime.");
            return;
        }

        // --- Step 2: Key generation ---
        int n   = p * q;
        int phi = (p - 1) * (q - 1);
        System.out.println("phi(n): " + phi);

        // Find smallest valid e: gcd(e, phi) == 1
        int e = 2;
        while (e < phi && gcd(e, phi) != 1) {
            e++;
        }

        if (e >= phi) {
            System.out.println("Error: Could not find a valid 'e' value.");
            return;
        }

        System.out.println("Chosen e: " + e + ", GCD(e, phi): " + gcd(e, phi));

        // Compute private exponent d
        int d = modInverse(e, phi);
        if (d == -1) {
            System.out.println("Error: Could not compute modular inverse.");
            return;
        }

        // --- Step 3: Display keys ---
        System.out.println("\n--- RSA Keys Generated ---");
        System.out.println("Public Key:  (n = " + n + ", e = " + e + ")");
        System.out.println("Private Key: (n = " + n + ", d = " + d + ")");

        // --- Step 4: Encrypt and decrypt a message ---
        System.out.print("\nEnter a number to encrypt (0 to " + (n - 1) + "): ");
        int m = input.nextInt();

        if (m < 0 || m >= n) {
            System.out.println("Error: Number must be in range 0 to " + (n - 1));
            return;
        }

        int encrypted = modPow(m, e, n);   // C = M^e mod n
        int decrypted = modPow(encrypted, d, n);  // M = C^d mod n

        System.out.println("Encrypted Number: " + encrypted);
        System.out.println("Decrypted Number: " + decrypted);

        input.close();
    }
}
