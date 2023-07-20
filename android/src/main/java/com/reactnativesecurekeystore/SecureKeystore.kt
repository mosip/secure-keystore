package com.reactnativesecurekeystore

interface SecureKeystore {
  fun generateKey(alias: String)
  fun generateKeyPair(alias: String): String
  fun encryptData(alias: String, data: String): String
  fun decryptData(alias: String, encryptedText: String): String
  fun removeKey(alias: String)
}
