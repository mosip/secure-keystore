package com.reactnativesecurekeystore

import java.security.KeyPair
import javax.crypto.SecretKey

interface KeyGenerator {
  fun generateKey(alias: String): SecretKey
  fun generateKeyPair(alias: String): KeyPair
}
