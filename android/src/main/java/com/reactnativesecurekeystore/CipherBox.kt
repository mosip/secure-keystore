package com.reactnativesecurekeystore

import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.SecretKey

interface CipherBox {
  fun initEncryptCipher(key: Key): Cipher
  fun encryptData(cipher: Cipher, data: String): EncryptedOutput

  fun initDecryptCipher(key: Key, encryptedOutput: EncryptedOutput): Cipher
  fun decryptData(cipher: Cipher, encryptedOutput: EncryptedOutput): ByteArray

  fun createSignature(key: PrivateKey): Signature
  fun sign(signature: Signature, data: String): String

  fun generateHmacSha(key: SecretKey, data: String): ByteArray
}
