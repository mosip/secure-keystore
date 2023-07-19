package com.reactnativesecurekeystore

import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key

interface CipherBox {
  fun encryptData(key: Key, data: String): EncryptedOutput
  fun decryptData(key: Key, encryptedText: String): ByteArray
}
