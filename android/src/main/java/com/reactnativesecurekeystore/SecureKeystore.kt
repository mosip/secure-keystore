package com.reactnativesecurekeystore

import javax.crypto.SecretKey

interface SecureKeystore {
  fun generateKey(alias: String): SecretKey
  fun generateKeyPair(alias: String)
  fun encryptData(alias: String, data: String): String
  fun decryptData(alias: String, encryptedText: String): String
  fun removeKey(alias: String)
}
