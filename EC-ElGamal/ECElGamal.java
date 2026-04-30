import java.util.*;

class Point {
    int x, y;
    boolean infinity;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.infinity = false;
    }

    Point() {
        this.infinity = true;
    }
}

public class ECElGamal {

    static int p, a, b;

    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++)
            if (n % i == 0) return false;
        return true;
    }

    static int mod(int x) {
        return ((x % p) + p) % p;
    }

    static int modInverse(int k) {
        k = mod(k);
        for (int i = 1; i < p; i++) {
            if ((k * i) % p == 1)
                return i;
        }
        return -1;
    }

    static Point add(Point P, Point Q) {

        if (P.infinity) return Q; // Q + O = Q
        if (Q.infinity) return P; // P + O = P

        if (P.x == Q.x && (P.y + Q.y) % p == 0) // opposite points (P + (-P) = O)
            return new Point();

        int lambda;

        if (P.x == Q.x && P.y == Q.y) { // λ = (3x^2 + a)/2y (doubling)

            int inv = modInverse(2 * P.y); // compute denominator inverse (2y)
            if (inv == -1) return new Point();

            lambda = mod((3 * P.x * P.x + a) * inv); // compute slope

        } else {

            int inv = modInverse(Q.x - P.x); // normal addition (λ = (y2 - y1)/(x2 - x1))
            if (inv == -1) return new Point();

            lambda = mod((Q.y - P.y) * inv);
        }

        int xr = mod(lambda * lambda - P.x - Q.x); // xr = λ^2 - x1 - x2
        int yr = mod(lambda * (P.x - xr) - P.y);   // yr = λ(x1 - xr) - y1

        return new Point(xr, yr);
    }

    static Point multiply(int k, Point P) { // kP = P + P + ...
        Point result = new Point();
        Point temp = P;

        while (k > 0) {
            if ((k & 1) == 1)
                result = add(result, temp);

            temp = add(temp, temp);
            k >>= 1;
        }
        return result;
    }

    static boolean isOnCurve(Point P) { // check if y^2 = x^3 + ax + b mod p
        if (P.infinity) return true;
        return mod(P.y * P.y) == mod(P.x * P.x * P.x + a * P.x + b);
    }

    // ---------------- COUNT #E ----------------
    static int countPoints() { // count all (x, y) valid pairs
        int count = 1;

        for (int x = 0; x < p; x++)
            for (int y = 0; y < p; y++)
                if (mod(y * y) == mod(x * x * x + a * x + b))
                    count++;

        return count;
    }

    static int pointOrder(Point G) { // Finds smallest k such that: kG = O

        Point temp = new Point(G.x, G.y);
        int order = 1;

        int limit = p + 1;

        while (order <= limit) {

            temp = add(temp, G);
            order++;

            if (temp.infinity)
                return order;
        }

        return order;
    }

    // ---------------- CHOOSE GENERATOR ----------------
    static Point choosePrimitivePoint() {

        Point best = null;
        int bestOrder = 0;

        for (int x = 0; x < p; x++) {
            for (int y = 0; y < p; y++) {

                Point P = new Point(x, y);

                if (!isOnCurve(P))
                    continue;

                int ord = pointOrder(P);

                if (ord > bestOrder && ord > 1) {
                    bestOrder = ord;
                    best = P;
                }
            }
        }

        System.out.println("Generator order: " + bestOrder);
        return best;
    }

    static void chooseCurve(Random rand) {

        while (true) {
            a = rand.nextInt(p);
            b = rand.nextInt(p);

            if (mod(4 * a * a * a + 27 * b * b) != 0)
                break;
        }

        System.out.println("Curve: y^2 = x^3 + " + a + "x + " + b);
    }

    static Point[] encrypt(Point M, int k, Point G, Point Q) {
        Point C1 = multiply(k, G);
        Point C2 = add(M, multiply(k, Q));
        return new Point[]{C1, C2};
    }

    static Point decrypt(Point C1, Point C2, int d) {
        Point S = multiply(d, C1);
        return add(C2, new Point(S.x, mod(-S.y)));
    }

    static int recover(Point M, Point G, int orderG) {
        for (int i = 1; i <= orderG; i++) {
            Point T = multiply(i, G);
            if (!T.infinity && T.x == M.x && T.y == M.y)
                return i;
        }
        return -1;
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        // PRIME INPUT
        while (true) {
            System.out.print("Enter prime p: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input");
                sc.next();
                continue;
            }

            p = sc.nextInt();

            if (isPrime(p)) break;

            System.out.println("Not prime");
        }

        // CURVE
        chooseCurve(rand);

        int orderE = countPoints();
        System.out.println("Total points (#E): " + orderE);

        // GENERATOR
        Point G = choosePrimitivePoint();
        int orderG = pointOrder(G);

        System.out.println("Generator: (" + G.x + "," + G.y + ")");

        // MESSAGE INPUT
        int m;
        while (true) {
            System.out.print("Enter message m: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input (must be integer)");
                sc.next();
                continue;
            }

            m = sc.nextInt();

            if (m <= 0) {
                System.out.println("Message must be positive");
                continue;
            }

            break;
        }

        m = m % orderG;
        if (m == 0) m = 1;

        Point M = multiply(m, G);

        // PRIVATE KEY INPUT
        int d;
        while (true) {
            System.out.print("Enter private key d: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input (must be integer)");
                sc.next();
                continue;
            }

            d = sc.nextInt();

            if (d <= 0 || d >= orderG) {
                System.out.println("d must be in range (1 to " + (orderG - 1) + ")");
                continue;
            }

            break;
        }

        Point Q = multiply(d, G);

        // RANDOM k
        int k = rand.nextInt(orderG - 1) + 1;

        // ENCRYPT
        Point[] cipher = encrypt(M, k, G, Q);

        // DECRYPT
        Point decrypted = decrypt(cipher[0], cipher[1], d);

        int recovered = recover(decrypted, G, orderG);

        // OUTPUT
        System.out.println("\n--- OUTPUT ---");

        System.out.println("Public Key: (" + Q.x + "," + Q.y + ")");
        System.out.println("Random k: " + k);

        System.out.println("Original Message Point: (" + M.x + "," + M.y + ")");

        System.out.println("C1: (" + cipher[0].x + "," + cipher[0].y + ")");
        System.out.println("C2: (" + cipher[1].x + "," + cipher[1].y + ")");

        System.out.println("Decrypted message: " + recovered);
    }
}
