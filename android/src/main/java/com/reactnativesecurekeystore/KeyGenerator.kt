package com.reactnativesecurekeystore

import java.security.KeyPair
import javax.crypto.SecretKey

interface KeyGenerator {
  fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?): SecretKey
  fun generateHmacKey(hmacKeyAlias: String): SecretKey
  fun generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): KeyPair
}
