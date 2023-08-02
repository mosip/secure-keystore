package com.reactnativesecurekeystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

const val KEY_PAIR_KEY_SIZE = 4096
const val KEY_AUTH_TIMEOUT = 10 * 60 * 1000

class KeyGeneratorImpl : com.reactnativesecurekeystore.KeyGenerator {
  private val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_TYPE)
  private val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_TYPE)

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
      getKeyPairGenSpecBuilder(alias).build()
    )

    return keyPairGenerator.generateKeyPair()
  }

  private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_DECRYPT or PURPOSE_ENCRYPT or PURPOSE_SIGN

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(ENCRYPTION_KEY_SIZE)
      .setBlockModes(BLOCK_MODE_GCM)
      .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
  }

  private fun getKeyPairGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_ENCRYPT or PURPOSE_DECRYPT or PURPOSE_SIGN or PURPOSE_VERIFY

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(KEY_PAIR_KEY_SIZE)
      .setDigests(DIGEST_SHA256, DIGEST_SHA512)
      .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_PKCS1)
      .setSignaturePaddings(SIGNATURE_PADDING_RSA_PKCS1)
      .setUserAuthenticationRequired(true)
  }
}
