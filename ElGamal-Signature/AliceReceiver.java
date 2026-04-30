import java.io.*;
import java.net.*;

public class AliceReceiver {

    static long p, alpha, beta;

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

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(5000);
        System.out.println("Alice waiting...");

        while (true) {
            Socket socket = server.accept();

            DataInputStream in = new DataInputStream(socket.getInputStream());

            // receive public key instead of hardcoding
            p     = Long.parseLong(in.readUTF());
            alpha = Long.parseLong(in.readUTF());
            beta  = Long.parseLong(in.readUTF());

            long x = Long.parseLong(in.readUTF());
            long r = Long.parseLong(in.readUTF());
            long s = Long.parseLong(in.readUTF());

            System.out.println("\nReceived:");
            System.out.println("p=" + p + " alpha=" + alpha + " beta=" + beta);
            System.out.println("x=" + x + " r=" + r + " s=" + s);

            // verify: β^r · r^s ≡ α^x mod p
            long left  = (modPow(beta, r, p) * modPow(r, s, p)) % p;
            long right = modPow(alpha, x, p);

            if (left == right)
                System.out.println("Valid Signature ✓");
            else
                System.out.println("Invalid Signature ✗");

            socket.close();
        }
    }
}
