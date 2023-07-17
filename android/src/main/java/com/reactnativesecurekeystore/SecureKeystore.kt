package com.reactnativesecurekeystore

import javax.crypto.SecretKey

interface SecureKeystore {
  fun generateKey(alias: String): SecretKey
  fun generateKeyPair(alias: String)
  fun removeKey(alias: String)
}
