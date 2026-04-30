import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class TripleDES {

    // ─── DES Standard Tables ──────────────────────────────────────────────────

    private static final int[] IP = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17,  9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    private static final int[] FP = {
        40,  8, 48, 16, 56, 24, 64, 32,
        39,  7, 47, 15, 55, 23, 63, 31,
        38,  6, 46, 14, 54, 22, 62, 30,
        37,  5, 45, 13, 53, 21, 61, 29,
        36,  4, 44, 12, 52, 20, 60, 28,
        35,  3, 43, 11, 51, 19, 59, 27,
        34,  2, 42, 10, 50, 18, 58, 26,
        33,  1, 41,  9, 49, 17, 57, 25
    };

    // Expansion table: 32 bits → 48 bits
    private static final int[] E = {
        32,  1,  2,  3,  4,  5,  4,  5,
         6,  7,  8,  9,  8,  9, 10, 11,
        12, 13, 12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21, 20, 21,
        22, 23, 24, 25, 24, 25, 26, 27,
        28, 29, 28, 29, 30, 31, 32,  1
    };

    // P-permutation after S-Box substitution
    private static final int[] P = {
        16,  7, 20, 21, 29, 12, 28, 17,
         1, 15, 23, 26,  5, 18, 31, 10,
         2,  8, 24, 14, 32, 27,  3,  9,
        19, 13, 30,  6, 22, 11,  4, 25
    };

    // PC1: 64-bit key → 56 bits
    private static final int[] PC1 = {
        57, 49, 41, 33, 25, 17,  9,
         1, 58, 50, 42, 34, 26, 18,
        10,  2, 59, 51, 43, 35, 27,
        19, 11,  3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
         7, 62, 54, 46, 38, 30, 22,
        14,  6, 61, 53, 45, 37, 29,
        21, 13,  5, 28, 20, 12,  4
    };

    // PC2: 56-bit combined halves → 48-bit subkey
    private static final int[] PC2 = {
        14, 17, 11, 24,  1,  5,  3, 28,
        15,  6, 21, 10, 23, 19, 12,  4,
        26,  8, 16,  7, 27, 20, 13,  2,
        41, 52, 31, 37, 47, 55, 30, 40,
        51, 45, 33, 48, 44, 49, 39, 56,
        34, 53, 46, 42, 50, 36, 29, 32
    };

    // 8 S-Boxes (each 4 rows × 16 cols)
    private static final int[][][] S = {
        {{14,4,13,1,2,15,11,8,3,10,6,12,5,9,0,7},
         {0,15,7,4,14,2,13,1,10,6,12,11,9,5,3,8},
         {4,1,14,8,13,6,2,11,15,12,9,7,3,10,5,0},
         {15,12,8,2,4,9,1,7,5,11,3,14,10,0,6,13}},
        {{15,1,8,14,6,11,3,4,9,7,2,13,12,0,5,10},
         {3,13,4,7,15,2,8,14,12,0,1,10,6,9,11,5},
         {0,14,7,11,10,4,13,1,5,8,12,6,9,3,2,15},
         {13,8,10,1,3,15,4,2,11,6,7,12,0,5,14,9}},
        {{10,0,9,14,6,3,15,5,1,13,12,7,11,4,2,8},
         {13,7,0,9,3,4,6,10,2,8,5,14,12,11,15,1},
         {13,6,4,9,8,15,3,0,11,1,2,12,5,10,14,7},
         {1,10,13,0,6,9,8,7,4,15,14,3,11,5,2,12}},
        {{7,13,14,3,0,6,9,10,1,2,8,5,11,12,4,15},
         {13,8,11,5,6,15,0,3,4,7,2,12,1,10,14,9},
         {10,6,9,0,12,11,7,13,15,1,3,14,5,2,8,4},
         {3,15,0,6,10,1,13,8,9,4,5,11,12,7,2,14}},
        {{2,12,4,1,7,10,11,6,8,5,3,15,13,0,14,9},
         {14,11,2,12,4,7,13,1,5,0,15,10,3,9,8,6},
         {4,2,1,11,10,13,7,8,15,9,12,5,6,3,0,14},
         {11,8,12,7,1,14,2,13,6,15,0,9,10,4,5,3}},
        {{12,1,10,15,9,2,6,8,0,13,3,4,14,7,5,11},
         {10,15,4,2,7,12,9,5,6,1,13,14,0,11,3,8},
         {9,14,15,5,2,8,12,3,7,0,4,10,1,13,11,6},
         {4,3,2,12,9,5,15,10,11,14,1,7,6,0,8,13}},
        {{4,11,2,14,15,0,8,13,3,12,9,7,5,10,6,1},
         {13,0,11,7,4,9,1,10,14,3,5,12,2,15,8,6},
         {1,4,11,13,12,3,7,14,10,15,6,8,0,5,9,2},
         {6,11,13,8,1,4,10,7,9,5,0,15,14,2,3,12}},
        {{13,2,8,4,6,15,11,1,10,9,3,14,5,0,12,7},
         {1,15,13,8,10,3,7,4,12,5,6,11,0,14,9,2},
         {7,11,4,1,9,12,14,2,0,6,10,13,15,3,5,8},
         {2,1,14,7,4,10,8,13,15,12,9,0,3,5,6,11}}
    };

    // Number of left shifts per round in the key schedule
    private static final int[] SHIFTS = {1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1};

    // Padding character used to align plaintext to 8-byte blocks
    private static final char PADDING_CHAR = '#';

    // ─── Utility Methods ──────────────────────────────────────────────────────

    // Convert a string to its binary representation (8 bits per character)
    private static String stringToBinary(String text) {
        String binary = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String bin = Integer.toBinaryString(c);
            while (bin.length() < 8) bin = "0" + bin;
            if (bin.length() > 8) bin = bin.substring(bin.length() - 8);
            binary += bin;
        }
        return binary;
    }

    // Convert a binary string back to text (8 bits per character)
    private static String binaryToString(String binary) {
        String text = "";
        for (int i = 0; i < binary.length(); i += 8) {
            String byteStr = binary.substring(i, i + 8);
            int charCode = 0;
            for (int j = 0; j < 8; j++)
                charCode = charCode * 2 + (byteStr.charAt(j) - '0');
            text += (char) charCode;
        }
        return text;
    }

    // Convert a binary string to uppercase hexadecimal
    private static String binaryToHex(String binary) {
        String hex = "";
        while (binary.length() % 4 != 0) binary = "0" + binary;
        for (int i = 0; i < binary.length(); i += 4) {
            String nibble = binary.substring(i, i + 4);
            int val = 0;
            for (int j = 0; j < 4; j++) val = val * 2 + (nibble.charAt(j) - '0');
            hex += val < 10 ? (char)('0' + val) : (char)('A' + (val - 10));
        }
        return hex;
    }

    // Convert a hexadecimal string to binary
    private static String hexToBinary(String hex) {
        String binary = "";
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            int val = (c >= '0' && c <= '9') ? c - '0' : c - 'A' + 10;
            String bin = "";
            for (int j = 0; j < 4; j++) { bin = (val % 2) + bin; val /= 2; }
            binary += bin;
        }
        return binary;
    }

    // Permute input bits according to a given DES table
    private static String permute(String input, int[] table, int n) {
        String output = "";
        for (int i = 0; i < n; i++)
            output += input.charAt(table[i] - 1);
        return output;
    }

    // XOR two binary strings of equal length
    private static String xor(String a, String b) {
        String result = "";
        for (int i = 0; i < a.length(); i++)
            result += (a.charAt(i) == b.charAt(i)) ? '0' : '1';
        return result;
    }

    // Cyclic left shift used in the DES key schedule
    private static String leftShift(String key, int shifts) {
        return key.substring(shifts) + key.substring(0, shifts);
    }

    // Pad plaintext with '#' characters to align to 8-byte block boundary
    private static String padText(String text) {
        String padded = text;
        while (padded.length() % 8 != 0) padded += PADDING_CHAR;
        return padded;
    }

    // ─── DES Key Schedule ─────────────────────────────────────────────────────

    // Derive 16 round subkeys from an 8-character key using PC1, shifts, and PC2
    private static String[] generateSubkeys(String key) {
        String keyBinary = stringToBinary(key);
        String key56 = permute(keyBinary, PC1, 56);

        String C = key56.substring(0, 28);
        String D = key56.substring(28);
        String[] subkeys = new String[16];

        for (int i = 0; i < 16; i++) {
            C = leftShift(C, SHIFTS[i]);
            D = leftShift(D, SHIFTS[i]);
            subkeys[i] = permute(C + D, PC2, 48);
        }
        return subkeys;
    }

    // ─── DES Feistel Function ────────────────────────────────────────────────

    // Apply S-Box substitution: split 48-bit input into 8 × 6-bit chunks
    private static String sBoxSubstitution(String input) {
        String output = "";
        for (int i = 0; i < 8; i++) {
            String chunk = input.substring(i * 6, (i + 1) * 6);
            int row = (chunk.charAt(0) - '0') * 2 + (chunk.charAt(5) - '0');
            int col = 0;
            for (int j = 1; j <= 4; j++) col = col * 2 + (chunk.charAt(j) - '0');
            int val = S[i][row][col];
            String binary = "";
            for (int j = 0; j < 4; j++) { binary = (val % 2) + binary; val /= 2; }
            output += binary;
        }
        return output;
    }

    // Feistel F-function: Expand → XOR with subkey → S-Box → P-permute
    private static String feistel(String right, String subkey) {
        String expanded   = permute(right, E, 48);
        String xored      = xor(expanded, subkey);
        String sBoxOutput = sBoxSubstitution(xored);
        return permute(sBoxOutput, P, 32);
    }

    // ─── Single DES Encrypt / Decrypt ────────────────────────────────────────

    // 16-round DES encryption on a 64-bit binary block
    private static String desEncrypt(String plaintext64, String key) {
        String[] subkeys = generateSubkeys(key);
        String permuted  = permute(plaintext64, IP, 64);
        String left      = permuted.substring(0, 32);
        String right     = permuted.substring(32);

        for (int i = 0; i < 16; i++) {
            String temp = right;
            right = xor(left, feistel(right, subkeys[i]));
            left  = temp;
        }
        return permute(right + left, FP, 64);
    }

    // 16-round DES decryption — same as encrypt but subkeys applied in reverse
    private static String desDecrypt(String ciphertext64, String key) {
        String[] subkeys = generateSubkeys(key);
        String permuted  = permute(ciphertext64, IP, 64);
        String left      = permuted.substring(0, 32);
        String right     = permuted.substring(32);

        for (int i = 15; i >= 0; i--) {
            String temp = right;
            right = xor(left, feistel(right, subkeys[i]));
            left  = temp;
        }
        return permute(right + left, FP, 64);
    }

    // ─── Triple DES (EDE) ────────────────────────────────────────────────────

    // 3DES encryption: E(K1) → D(K2) → E(K3) per 64-bit block, output as hex
    public static String tripleDesEncrypt(String plaintext, String k1, String k2, String k3) {
        String binaryText = stringToBinary(plaintext);
        while (binaryText.length() % 64 != 0) binaryText += "00100011"; // pad '#'

        String resultBinary = "";
        for (int i = 0; i < binaryText.length(); i += 64) {
            String block = binaryText.substring(i, i + 64);
            String step1 = desEncrypt(block, k1);
            String step2 = desDecrypt(step1,  k2);
            String step3 = desEncrypt(step2,  k3);
            resultBinary += step3;
        }
        return binaryToHex(resultBinary);
    }

    // 3DES decryption: D(K3) → E(K2) → D(K1) per 64-bit block, strips padding
    public static String tripleDesDecrypt(String ciphertextHex, String k1, String k2, String k3) {
        String binaryText  = hexToBinary(ciphertextHex);
        String resultBinary = "";

        for (int i = 0; i < binaryText.length(); i += 64) {
            String block = binaryText.substring(i, i + 64);
            String step1 = desDecrypt(block, k3);
            String step2 = desEncrypt(step1,  k2);
            String step3 = desDecrypt(step2,  k1);
            resultBinary += step3;
        }

        String decrypted = binaryToString(resultBinary);
        // Strip trailing padding characters
        while (decrypted.length() > 0 &&
               decrypted.charAt(decrypted.length() - 1) == PADDING_CHAR)
            decrypted = decrypted.substring(0, decrypted.length() - 1);

        return decrypted;
    }

    // ─── Meet-in-the-Middle Attack ───────────────────────────────────────────

    /**
     * MITM attack: recovers the first byte of K1, K2, K3.
     * Phase 1 — build forward table: D(K2, E(K1, P)) for all 256² first-byte combinations.
     * Phase 2 — search backward: D(K3, C) for all 256 first-byte values, match against table.
     */
    public static String[] mitmAttack(String plaintext, String ciphertextHex,
                                      String k1, String k2, String k3) {

        System.out.println("\n=== MITM ATTACK ===");
        System.out.println("Recovering first byte of K1, K2, K3...");

        String padded        = padText(plaintext);
        String firstBlock    = stringToBinary(padded).substring(0, 64);
        String firstCipher   = hexToBinary(ciphertextHex).substring(0, 64);
        String k1Base = k1.substring(1), k2Base = k2.substring(1), k3Base = k3.substring(1);

        // Phase 1: forward table indexed by D(K2, E(K1, P))
        System.out.println("Phase 1: Computing intermediate states for all K1,K2 possibilities...");
        HashMap<String, ArrayList<String[]>> forwardTable = new HashMap<>();

        for (int i = 0; i <= 255; i++) {
            String testK1   = (char) i + k1Base;
            String afterK1  = desEncrypt(firstBlock, testK1);

            for (int j = 0; j <= 255; j++) {
                String testK2        = (char) j + k2Base;
                String intermediate  = desDecrypt(afterK1, testK2);
                forwardTable.computeIfAbsent(intermediate, x -> new ArrayList<>())
                             .add(new String[]{testK1, testK2});
            }
        }

        // Phase 2: search backward from ciphertext with K3
        System.out.println("Phase 2: Searching backward from ciphertext...");
        ArrayList<String[]> successfulKeys = new ArrayList<>();

        for (int ascii3 = 0; ascii3 <= 255; ascii3++) {
            String testK3        = (char) ascii3 + k3Base;
            String intermediate  = desDecrypt(firstCipher, testK3);

            if (forwardTable.containsKey(intermediate)) {
                for (String[] keyPair : forwardTable.get(intermediate)) {
                    String fullCipherTest = tripleDesEncrypt(padded, keyPair[0], keyPair[1], testK3);
                    if (fullCipherTest.equals(ciphertextHex))
                        successfulKeys.add(new String[]{keyPair[0], keyPair[1], testK3});
                }
            }
        }

        // Check if original keys were recovered
        String[] result = {"", "", ""};
        boolean foundOriginal = false;
        for (String[] triple : successfulKeys) {
            if (triple[0].equals(k1) && triple[1].equals(k2) && triple[2].equals(k3)) {
                result = triple;
                foundOriginal = true;
                break;
            }
        }

        if (foundOriginal)              System.out.println("\nKeys recovered (original)!");
        else if (!successfulKeys.isEmpty()) System.out.println("\nKeys recovered (alternative)!");
        else                            System.out.println("\n=== MITM failed! ===");

        System.out.println("K1: " + result[0]);
        System.out.println("K2: " + result[1]);
        System.out.println("K3: " + result[2]);
        return result;
    }

    // ─── Main ────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- Part One: Encryption & Decryption ---
        System.out.println("\n=== PART ONE ===");
        System.out.print("Enter plaintext: ");
        String plaintext = sc.nextLine();

        String k1, k2, k3;
        do { System.out.print("Enter Key 1 (8 chars): "); k1 = sc.nextLine();
             if (k1.length() != 8) System.out.println("ERROR: Must be 8 chars!"); } while (k1.length() != 8);
        do { System.out.print("Enter Key 2 (8 chars): "); k2 = sc.nextLine();
             if (k2.length() != 8) System.out.println("ERROR: Must be 8 chars!"); } while (k2.length() != 8);
        do { System.out.print("Enter Key 3 (8 chars): "); k3 = sc.nextLine();
             if (k3.length() != 8) System.out.println("ERROR: Must be 8 chars!"); } while (k3.length() != 8);

        String paddedText = padText(plaintext);
        if (!paddedText.equals(plaintext))
            System.out.println("Plaintext padded to 8-char boundary: \"" + paddedText + "\"");

        System.out.println("\n=== ENCRYPTION ===");
        System.out.println("Keys: K1=\"" + k1 + "\", K2=\"" + k2 + "\", K3=\"" + k3 + "\"");
        String ciphertextHex = tripleDesEncrypt(paddedText, k1, k2, k3);
        System.out.println("Ciphertext (HEX): \"" + ciphertextHex + "\"");

        System.out.println("\n=== DECRYPTION ===");
        String decrypted = tripleDesDecrypt(ciphertextHex, k1, k2, k3);
        System.out.println("Decrypted: \"" + decrypted + "\"");

        // --- Part Two: MITM Attack ---
        System.out.println("\n=== PART TWO: MITM ATTACK ===");
        System.out.println("Unknown first character of each key (first 8 bits):");
        System.out.println("K1: ?" + k1.substring(1));
        System.out.println("K2: ?" + k2.substring(1));
        System.out.println("K3: ?" + k3.substring(1));

        String[] recovered = mitmAttack(plaintext, ciphertextHex, k1, k2, k3);

        if (!recovered[0].isEmpty()) {
            System.out.println("\n=== MITM ATTACK RESULTS ===");
            System.out.println("Recovered K1: \"" + recovered[0] + "\"");
            System.out.println("Recovered K2: \"" + recovered[1] + "\"");
            System.out.println("Recovered K3: \"" + recovered[2] + "\"");
            System.out.println("Original  K1: \"" + k1 + "\"");
            System.out.println("Original  K2: \"" + k2 + "\"");
            System.out.println("Original  K3: \"" + k3 + "\"");

            if (recovered[0].equals(k1) && recovered[1].equals(k2) && recovered[2].equals(k3)) {
                System.out.println("\n=== MITM ATTACK SUCCESSFUL! ===");
                System.out.println("First 8 bits recovered correctly!");
            }
        } else {
            System.out.println("\n=== MITM ATTACK FAILED! ===");
        }

        sc.close();
    }
}
