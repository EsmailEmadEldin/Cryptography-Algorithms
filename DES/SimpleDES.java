import java.util.Scanner;

public class SimpleDES {

    // Initial permutation table (first 16 for simplicity)
    // This table defines how bits are rearranged during the initial permutation
    static int[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9 , 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };

    // Final permutation table (same size as IP)
    // This table defines how bits are rearranged after the encryption process
    static int[] FP = {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9 , 49, 17, 57, 25
    };

    // Convert string to 64-bit block
    // This method takes an 8-character string and converts it into a 64-bit binary block
    static void stringToBlock(String input, int[] block) {
        for (int i = 0; i < 8; i++) {
            char ch = input.charAt(i); // Split each character into its 8-bit binary representation
            for (int j = 0; j < 8; j++) {
                block[i * 8 + j] = (ch >> (7 - j)) & 1;
            }
        }
    }

    // Convert 64-bit block to string
    // Converts the 64-bit binary block back to a string of characters
    static void blockToString(int[] block, char[] output) {
        for (int i = 0; i < 8; i++) {
            char ch = 0; // Rebuild each character from the 8 bits in the block
            for (int j = 0; j < 8; j++) {
                ch |= block[i * 8 + j] << (7 - j);
            }
            output[i] = ch;
        }
        output[8] = '\0'; // Null-terminate the string
    }

    // Permute a block using a given table
    // This method permutes the input block according to the provided table (IP or FP)
    static void permute(int[] input, int[] output, int[] table) {
        for (int i = 0; i < 64; i++) { // Permute according to the table's indices
            output[i] = input[table[i] - 1];
        }
    }

    // A simple XOR-based Feistel function for demo
    // This function simulates the Feistel function used in DES (using XOR for simplicity)
    static void feistelFunction(int[] half, int[] key, int[] output) {
        for (int i = 0; i < 32; i++) {
            output[i] = half[i] ^ key[i % 32]; // XOR each bit with the key
        }
    }

    // Encrypt using a simplified one-round DES-like algorithm
    // This method performs encryption based on the given plaintext and key
    static void encrypt(String plaintext, String key, char[] ciphertext) {
        int[] block = new int[64], permuted = new int[64];
        int[] L = new int[32], R = new int[32], newR = new int[32], combined = new int[64];
        int[] keyBlock = new int[64];

        stringToBlock(plaintext, block);  // Convert plaintext to a binary block
        stringToBlock(key, keyBlock);     // Convert key to a binary block

        permute(block, permuted, IP);     // Apply the initial permutation

        // Split into L and R
        System.arraycopy(permuted, 0, L, 0, 32);
        System.arraycopy(permuted, 32, R, 0, 32);

        feistelFunction(R, keyBlock, newR); // Apply the Feistel function on the right half

        // XOR left with result
        for (int i = 0; i < 32; i++) {
            newR[i] = L[i] ^ newR[i];
        }

        // Combine R and newR (swap halves)
        System.arraycopy(R, 0, combined, 0, 32);    // Original right half
        System.arraycopy(newR, 0, combined, 32, 32); // New right half (result of XOR)

        int[] finalBlock = new int[64];  // Apply final permutation
        permute(combined, finalBlock, FP);

        blockToString(finalBlock, ciphertext); // Convert the final block to a string
    }

    // Decrypt using a simplified one-round DES-like algorithm
    // This method performs decryption based on the given ciphertext and key
    static void decrypt(String ciphertext, String key, char[] plaintext) {
        int[] block = new int[64], permuted = new int[64];
        int[] L = new int[32], R = new int[32], newL = new int[32], combined = new int[64];
        int[] tempKeyBlock = new int[64];
        int[] keyBlock = new int[32];

        stringToBlock(ciphertext, block);   // Convert ciphertext to a binary block
        stringToBlock(key, tempKeyBlock);   // Convert key to a binary block
        System.arraycopy(tempKeyBlock, 0, keyBlock, 0, 32); // Use first 32 bits of key

        permute(block, permuted, IP); // Apply the initial permutation

        // Split into L and R
        System.arraycopy(permuted, 0, L, 0, 32);
        System.arraycopy(permuted, 32, R, 0, 32);

        // Decrypt step (reverse of encrypt)
        feistelFunction(L, keyBlock, newL); // F(L), not R

        // R' = L XOR F(L)
        for (int i = 0; i < 32; i++) {
            newL[i] = R[i] ^ newL[i];
        }

        // Combine halves in reverse order (undo swap)
        System.arraycopy(newL, 0, combined, 0, 32); // Original L
        System.arraycopy(L, 0, combined, 32, 32);   // Original R

        int[] finalBlock = new int[64];
        permute(combined, finalBlock, FP); // Apply final permutation

        blockToString(finalBlock, plaintext); // Convert the final block to a string
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String plaintext, key;

        System.out.print("Enter 8-character plaintext: ");
        plaintext = scanner.nextLine();

        System.out.print("Enter 8-character key: ");
        key = scanner.nextLine();

        // Ensure that the plaintext and key are exactly 8 characters long
        if (plaintext.length() != 8 || key.length() != 8) {
            System.out.println("Error: Plaintext and key must be exactly 8 characters long.");
            return;
        }

        // Buffers for encrypted and decrypted text
        char[] encrypted = new char[9];
        char[] decrypted = new char[9];

        encrypt(plaintext, key, encrypted);                  // Encrypt the plaintext
        decrypt(new String(encrypted), key, decrypted);      // Decrypt the ciphertext

        System.out.println("Original Text :       " + plaintext);

        // Print encrypted text in hexadecimal format
        System.out.print("Encrypted Text (Hex): ");
        for (int i = 0; i < 8; i++) {
            System.out.printf("%02x ", (int) encrypted[i] & 0xFF); // Print each byte as hex
        }
        System.out.println();

        // Print decrypted text
        System.out.println("Decrypted Text:       " + new String(decrypted));

        scanner.close();
    }
}
