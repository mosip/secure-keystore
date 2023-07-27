package com.reactnativesecurekeystore

import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.PrivateKey

interface CipherBox {
  fun encryptData(key: Key, data: String): EncryptedOutput
  fun decryptData(key: Key, encryptedText: EncryptedOutput): ByteArray
  fun sign(key: PrivateKey, data: String): ByteArray
  fun generateHmacSha(data: String): ByteArray
}
