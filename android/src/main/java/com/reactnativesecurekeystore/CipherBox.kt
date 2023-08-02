package com.reactnativesecurekeystore

import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.PrivateKey
import java.security.Signature

interface CipherBox {
  fun encryptData(key: Key, data: String): EncryptedOutput
  fun decryptData(key: Key, encryptedOutput: EncryptedOutput): ByteArray
  fun createSignature(key: PrivateKey, data: String): Signature
  fun generateHmacSha(key: Key, data: String): ByteArray
}
