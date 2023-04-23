package rsa.keys.generator

import me.victoralan.crypto.SHA3
import rsa.keys.RSAKeyPair
import rsa.keys.RSAPrivateKey
import rsa.keys.RSAPublicKey
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*


class RSAKeyGen {

    companion object{
        fun generateKeyPair(random: Random = SecureRandom(), bitLength: Int): RSAKeyPair {
            // Choose two large prime numbers, p and q.
            var p: BigInteger = BigInteger.probablePrime(bitLength, random)
            var q: BigInteger = BigInteger.probablePrime(bitLength, random)
            var areNumbersEffective = false
            // Check that (p - q) has a bitLength of at least bigLength/2
            while (!areNumbersEffective){
                if ( (p - q).bitLength() > bitLength/1.1){
                    areNumbersEffective = true
                } else{
                    p = BigInteger.probablePrime(bitLength, random)
                    q = BigInteger.probablePrime(bitLength, random)
                }
            }

            // Calculate n = pq
            val n: BigInteger = p*q

            // Calculate phi(n) = (p-1)(q-1)
            val phi = (p-BigInteger.ONE)*(q-BigInteger.ONE)

            // Choose an integer e such that 1 < e < phi(n) and gcd(e,phi(n)) = 1. e is the public key exponent.
            val e: BigInteger = generatePublicExponent(phi, bitLength, random)

            // Calculate d such that d ≡ e^-1 (mod phi(n)). d is the private key exponent.
            val d: BigInteger = e.modPow(BigInteger("-1"), phi)

            // The public key is (n,e), and the private key is (n,d).
            val publicKey: RSAPublicKey = RSAPublicKey(n, e)
            val privateKey: RSAPrivateKey = RSAPrivateKey(n, d)

            return RSAKeyPair(publicKey, privateKey)
        }
        fun generateKeyPair(passphrase: String, bitLength: Int) : RSAKeyPair{
            return generateKeyPair(Random(BigInteger(SHA3.hashString(passphrase)).toLong()), bitLength)
        }
        private fun generatePublicExponent(phi: BigInteger, bitLength: Int, random: Random): BigInteger {
            var e: BigInteger
            do {
                e = BigInteger(bitLength, random)
            } while (e <= BigInteger.TWO || e >= phi || !e.isProbablePrime(80) || !phi.gcd(e).equals(BigInteger.ONE))
            return e
        }

    }


}