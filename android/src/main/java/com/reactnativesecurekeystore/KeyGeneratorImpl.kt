package com.reactnativesecurekeystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyGeneratorImpl: com.reactnativesecurekeystore.KeyGenerator {
  private val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
  private val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_TYPE)

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String): SecretKey {
    keyGenerator.init(
      getKeyGenSpecBuilder(alias).build()
    )

    return keyGenerator.generateKey()
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String): KeyPair {
    keyPairGenerator.initialize(
      getKeyGenSpecBuilder(alias).build()
    )

    return keyPairGenerator.generateKeyPair()
  }

  private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(ENCRYPTION_KEY_SIZE)
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
  }
}
