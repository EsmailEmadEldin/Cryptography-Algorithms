# 🔐 Cryptography-Algorithms — Classic Cryptosystems in Java

A collection of **eight cryptographic algorithms and attacks** implemented from scratch in Java — no external libraries, no `javax.crypto`. Every primitive (S-boxes, Galois Field arithmetic, elliptic curve point operations, modular inverses) is hand-coded to demonstrate the underlying mathematics of each system.

---

## 📌 About

This project was built across several cryptography assignments at Cairo University. Each algorithm is implemented structurally: tables are defined explicitly, mathematical operations are written step-by-step, and no standard cryptography API is used. The collection covers **symmetric**, **asymmetric**, and **elliptic curve** cryptography, plus two attack implementations (Meet-in-the-Middle and ElGamal signature forgery).

---

## 📁 Project Structure

```
Cryptography-Algorithms/
│
├── DES/
│   └── SimpleDES.java              # Simplified 1-round DES with full IP/FP tables
│
├── TripleDES-MITM/
│   └── TripleDES.java              # Full 16-round 3DES (EDE) + Meet-in-the-Middle attack
│
├── LFSR/
│   └── Main.java                   # Linear Feedback Shift Register stream cipher
│
├── AES/
│   └── AES.java                    # Full AES-128 (10 rounds, all 4 transformations)
│
├── RSA/
│   └── RSA.java                    # RSA key generation, encryption, decryption
│
├── ElGamal/
│   ├── Alice.java                  # Sender: encrypts and sends over socket
│   └── Bob.java                    # Receiver: sets up keys, decrypts
│
├── ElGamal-Signature/
│   ├── BobSender.java              # Signs a message and sends over socket
│   ├── AliceReceiver.java          # Verifies the ElGamal signature
│   └── OscarAttack.java            # Existential forgery attack on ElGamal signature
│
├── EC-ElGamal/
│   └── ECElGamal.java              # Elliptic Curve ElGamal over GF(p)
│
├── README.md                       # Project documentation
├── LICENSE                         # MIT License
└── .gitignore                      # Ignores compiled .class files
```

---

## 🛠️ Algorithms Overview

### 1. 🔷 DES — Simplified (`DES/SimpleDES.java`)

A 1-round DES-like cipher using the full standard IP and FP permutation tables and a Feistel function built on XOR. Demonstrates the structural skeleton of DES without the 16-round key schedule.

| Detail | Value |
|--------|-------|
| Block size | 64 bits (8 chars) |
| Key size | 64 bits (8 chars) |
| Rounds | 1 (simplified) |
| Input | Console — 8-char plaintext and key |
| Output | Encrypted hex + decrypted text |

---

### 2. 🔷 Triple DES + MITM Attack (`TripleDES-MITM/TripleDES.java`)

A full 16-round DES implementation (with all standard tables: IP, FP, E, P, PC1, PC2, 8 S-boxes, key schedule) composed three times in EDE mode. Includes a **Meet-in-the-Middle attack** that recovers the first byte of each of the three keys.

| Detail | Value |
|--------|-------|
| Block size | 64 bits |
| Key size | 3 × 64 bits |
| Mode | EDE (Encrypt-Decrypt-Encrypt) |
| Padding | `#` character to 8-byte boundary |
| Attack | MITM — Phase 1 builds forward table over 256² (K1,K2) pairs; Phase 2 searches backward over 256 K3 values |
| Input | Console — plaintext + 3 keys (each 8 chars) |
| Output | Ciphertext (hex) + decrypted text + recovered keys |

---

### 3. 🔷 LFSR Stream Cipher (`LFSR/Main.java`)

A Linear Feedback Shift Register (LFSR) stream cipher. The user supplies a binary seed, tap positions, and the number of keystream bits to generate. Encryption and decryption are both a single XOR with the keystream.

| Detail | Value |
|--------|-------|
| Key | Binary seed + tap positions |
| Keystream | XOR-based, length configurable |
| Input | Console — seed, taps, bit count, binary plaintext |
| Output | Keystream + ciphertext + decrypted text |

---

### 4. 🔷 AES-128 (`AES/AES.java`)

A complete manual AES-128 implementation with all four transformations and a full key schedule.

| Detail | Value |
|--------|-------|
| Block size | 128 bits |
| Key size | 128 bits |
| Rounds | 10 |
| Transformations | SubBytes, ShiftRows, MixColumns, AddRoundKey (all inverted for decryption) |
| GF arithmetic | GF(2⁸) multiplication with irreducible polynomial `0x11b` |
| S-Box | Hardcoded 256-entry lookup table + inverse |
| Input | Console — up to 16-char plaintext, hardcoded key |
| Output | Ciphertext (hex) + decrypted text |

---

### 5. 🔷 RSA (`RSA/RSA.java`)

RSA key generation, encryption, and decryption using trial-division primality testing, the iterative Euclidean GCD, the Extended Euclidean modular inverse, and fast modular exponentiation via repeated squaring.

| Detail | Value |
|--------|-------|
| Key generation | User supplies two primes p and q |
| Public exponent | Smallest e coprime to φ(n) |
| Private exponent | Modular inverse of e mod φ(n) |
| Operations | C = Mᵉ mod n, M = Cᵈ mod n |
| Input | Console — p, q, message integer |
| Output | Public key, private key, encrypted and decrypted values |

---

### 6. 🔷 ElGamal Encryption (`ElGamal/`)

A networked ElGamal encryption system. Bob acts as the server — he validates a prime `p`, a primitive root `α`, and his private key `d`, then broadcasts the public key `β = αᵈ mod p` over a TCP socket. Alice connects as a client, encrypts a plaintext message using a random ephemeral key, and sends the ciphertext pair `(k_E, y)`. Bob decrypts using his private key.

| Detail | Value |
|--------|-------|
| Transport | TCP sockets (localhost:12345) |
| Public key | `(p, α, β)` where `β = αᵈ mod p` |
| Encryption | `k_E = αⁱ mod p`, `k_M = βⁱ mod p`, `y = x · k_M mod p` |
| Decryption | `k_M = k_Eᵈ mod p`, `x = y · k_M⁻¹ mod p` |
| Run order | Start `Bob.java` first, then `Alice.java` |

---

### 7. 🔷 ElGamal Digital Signature + Forgery Attack (`ElGamal-Signature/`)

An ElGamal digital signature scheme implemented over a TCP socket, with an existential forgery attack.

**`BobSender.java`** — signs a randomly chosen message `x` using private key `d`:
- Picks random `k` coprime to `p-1`
- Computes `r = αᵏ mod p`
- Computes `s = (x - d·r) · k⁻¹ mod (p-1)`
- Sends `(p, α, β, x, r, s)` to Alice

**`AliceReceiver.java`** — verifies: `βʳ · rˢ ≡ αˣ mod p`

**`OscarAttack.java`** — existential forgery (no knowledge of `d`):
- Picks random `i`, `j` coprime to `p-1`
- Forges `r = αⁱ · βʲ mod p`
- Forges `s = -r · j⁻¹ mod (p-1)`
- Forges `x = s · i mod (p-1)`
- Sends forged `(x, r, s)` — passes Alice's verification

| Detail | Value |
|--------|-------|
| Transport | TCP sockets (localhost:5000) |
| Run order (normal) | Start `AliceReceiver`, then `BobSender` |
| Run order (attack) | Start `AliceReceiver`, then `OscarAttack` |

---

### 8. 🔷 EC-ElGamal (`EC-ElGamal/ECElGamal.java`)

ElGamal encryption lifted onto an elliptic curve over GF(p). The curve parameters are chosen randomly (non-singular check enforced), the generator point is selected as the point with the highest order, and the message integer is mapped to a curve point via scalar multiplication.

| Detail | Value |
|--------|-------|
| Curve form | `y² = x³ + ax + b mod p` |
| Field | GF(p) for user-supplied prime p |
| Point arithmetic | Addition + doubling via slope formula; point at infinity handled |
| Key generation | Private key `d`, public key `Q = dG` |
| Encryption | `C1 = kG`, `C2 = M + kQ` |
| Decryption | `M = C2 - d·C1` |
| Input | Console — prime p, message integer, private key d |
| Output | Public key, ciphertext points, decrypted message integer |

---

## ▶️ How to Run

### Prerequisites
- Java JDK 8 or later (`javac` and `java` on PATH)

### Compile & Run (single-file algorithms)
```bash
# Example: AES
javac AES/AES.java -d out/
java -cp out AES

# Example: RSA
javac RSA/RSA.java -d out/
java -cp out RSA
```

### Compile & Run (networked algorithms — ElGamal)
```bash
# Terminal 1 — start server (Bob) first
javac ElGamal/Bob.java -d out/
java -cp out Bob

# Terminal 2 — connect client (Alice)
javac ElGamal/Alice.java -d out/
java -cp out Alice
```

### Compile & Run (ElGamal Signature)
```bash
# Terminal 1 — start receiver first
javac ElGamal-Signature/AliceReceiver.java -d out/
java -cp out AliceReceiver

# Terminal 2 — send signed message (Bob) or run the attack (Oscar)
javac ElGamal-Signature/BobSender.java -d out/
java -cp out BobSender

# — OR —
javac ElGamal-Signature/OscarAttack.java -d out/
java -cp out OscarAttack
```

---

## 🧠 Concepts Demonstrated

- **Feistel network** — DES and 3DES both use a split-XOR-swap structure; decryption reverses subkey order only
- **S-Box substitution** — DES uses 8 hardcoded 4×16 tables; AES uses a 256-entry algebraic S-Box over GF(2⁸)
- **Key schedule** — DES derives 16 subkeys via PC1, cyclic left shifts, and PC2; AES expands 16 bytes into 44 words using RotWord, SubWord, and Rcon
- **GF(2⁸) arithmetic** — AES MixColumns uses carry-less multiplication modulo `0x11b`
- **Meet-in-the-Middle attack** — reduces 3DES brute-force from 2¹⁶⁸ to 2¹¹² by building a forward table on (K1,K2) and searching backward on K3
- **LFSR stream cipher** — linear recurrence over GF(2); security depends entirely on tap positions and seed secrecy
- **RSA trapdoor** — factoring hardness; private key computed via Extended Euclidean Algorithm
- **ElGamal semantic security** — based on the Decisional Diffie-Hellman assumption over a prime-order group
- **ElGamal existential forgery** — Oscar forges a valid `(x, r, s)` triple without knowing the private key, exploiting the signature equation algebraically
- **Elliptic curve group law** — point addition and doubling derived from chord-and-tangent geometry; point at infinity acts as the group identity
- **Networked cryptography** — ElGamal and its signature scheme run over TCP sockets, separating key setup, encryption, and decryption into independent processes

---

## 👤 Author

**Esmail Emad El-Din Mohamed**
Cairo University — Computer Science & Artificial Intelligence

---

## 📜 License

This project is open source and available under the [MIT License](LICENSE).
