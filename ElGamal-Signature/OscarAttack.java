import java.io.*;
import java.net.*;
import java.util.Random;

public class OscarAttack {

    static long p     = 29;
    static long alpha = 2;

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

    static long getVal() {
        long v;
        do {
            v = rand.nextInt(27) + 1;
        } while (gcd(v, p - 1) != 1);
        return v;
    }

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 5000);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Oscar fabricates a fake public key (does not know Bob's real d)
        long d_fake = rand.nextInt(10) + 2;
        long beta   = modPow(alpha, d_fake, p);

        long i = getVal();
        long j = getVal();

        // forge r = α^i · β^j mod p
        long r = (modPow(alpha, i, p) * modPow(beta, j, p)) % p;

        // forge s = -r · j⁻¹ mod (p-1)
        long jInv = modInverse(j, p - 1);
        long s    = ((-r) % (p - 1) + (p - 1)) % (p - 1);
        s = (s * jInv) % (p - 1);

        // forge x = s · i mod (p-1)
        long x = (s * i) % (p - 1);

        // send forged public parameters
        out.writeUTF(String.valueOf(p));
        out.writeUTF(String.valueOf(alpha));
        out.writeUTF(String.valueOf(beta));

        // send forged signature
        out.writeUTF(String.valueOf(x));
        out.writeUTF(String.valueOf(r));
        out.writeUTF(String.valueOf(s));

        System.out.println("Oscar forged x=" + x + " r=" + r + " s=" + s);

        socket.close();
    }
}
