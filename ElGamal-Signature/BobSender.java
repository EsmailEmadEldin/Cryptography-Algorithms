import java.io.*;
import java.net.*;
import java.util.Random;

public class BobSender {

    static long p     = 29;
    static long alpha = 2;
    static long d     = 12; // private key

    static Random rand = new Random();

    static long modPow(long base, long exp, long mod) {
        long result = 1;
        base %= mod;

        while (exp > 0) {
            if (exp % 2 == 1)
                result = (result * base) % mod;

            base = (base * base) % mod;
            exp /= 2;
        }
        return result;
    }

    static long modInverse(long a, long m) {
        a %= m;
        for (long x = 1; x < m; x++) {
            if ((a * x) % m == 1)
                return x;
        }
        return 1;
    }

    static long gcd(long a, long b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    static long getK() {
        long k;
        do {
            k = rand.nextInt(27) + 1;
        } while (gcd(k, p - 1) != 1);
        return k;
    }

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 5000);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        long beta = modPow(alpha, d, p); // public key

        long x    = rand.nextInt(20) + 1;
        long k    = getK();

        long r    = modPow(alpha, k, p);
        long kInv = modInverse(k, p - 1);

        // s = (x - d·r) · k⁻¹ mod (p-1)
        long s = ((x - d * r) % (p - 1) + (p - 1)) % (p - 1);
        s = (s * kInv) % (p - 1);

        // send public parameters
        out.writeUTF(String.valueOf(p));
        out.writeUTF(String.valueOf(alpha));
        out.writeUTF(String.valueOf(beta));

        // send signed message
        out.writeUTF(String.valueOf(x));
        out.writeUTF(String.valueOf(r));
        out.writeUTF(String.valueOf(s));

        System.out.println("Bob sent x=" + x + " r=" + r + " s=" + s);

        socket.close();
    }
}
