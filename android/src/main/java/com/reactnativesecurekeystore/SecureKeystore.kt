package com.reactnativesecurekeystore

interface SecureKeystore {
  fun hasAlias(alias: String): Boolean
  fun generateKey(alias: String)
  fun generateKeyPair(alias: String): String
  fun encryptData(alias: String, data: String): String
  fun decryptData(alias: String, encryptedText: String): String
  fun generateHmacSha(alias: String, data: String): String
  fun removeKey(alias: String)
  fun sign(alias: String, data: String): String
}
