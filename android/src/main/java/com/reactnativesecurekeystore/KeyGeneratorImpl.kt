package com.reactnativesecurekeystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.lang.Exception
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

const val KEY_PAIR_KEY_SIZE = 4096

class KeyGeneratorImpl : com.reactnativesecurekeystore.KeyGenerator {
  private val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_TYPE)
  private val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_TYPE)
  private val hmacKeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_HMAC_SHA256, KEYSTORE_TYPE)

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?): SecretKey {
    val keySpecBuilder = getKeyGenSpecBuilder(alias)

    if (isAuthRequired) {
      setUserAuth(keySpecBuilder, authTimeout)
    }

    keyGenerator.init(keySpecBuilder.build())

    return keyGenerator.generateKey()
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): KeyPair {
    val keySpecBuilder = getKeyPairGenSpecBuilder(alias)

    if (isAuthRequired) {
      setUserAuth(keySpecBuilder, authTimeout)
    }

    keyPairGenerator.initialize(keySpecBuilder.build())

    return keyPairGenerator.generateKeyPair()
  }

  override fun generateHmacKey(hmacKeyAlias: String): SecretKey {
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(hmacKeyAlias, PURPOSE_SIGN).build()

    hmacKeyGenerator.init(keyGenParameterSpec)

    return hmacKeyGenerator.generateKey()
  }

  private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_DECRYPT or PURPOSE_ENCRYPT or PURPOSE_SIGN

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(ENCRYPTION_KEY_SIZE)
      .setBlockModes(BLOCK_MODE_GCM)
      .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
      .setUserAuthenticationRequired(true)
  }

  private fun getKeyPairGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_ENCRYPT or PURPOSE_DECRYPT or PURPOSE_SIGN or PURPOSE_VERIFY

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(KEY_PAIR_KEY_SIZE)
      .setDigests(DIGEST_SHA256, DIGEST_SHA512)
      .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_PKCS1)
      .setSignaturePaddings(SIGNATURE_PADDING_RSA_PKCS1)
  }

  private fun setUserAuth(
    builder: KeyGenParameterSpec.Builder, authTimeout: Int?
  ) {
    builder.setUserAuthenticationRequired(true)

    if (authTimeout != null) {
      setAuthTimeout(builder, authTimeout)
    }
  }

  private fun setAuthTimeout(builder: KeyGenParameterSpec.Builder, authTimeout: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      builder.setUserAuthenticationParameters(authTimeout, AUTH_BIOMETRIC_STRONG)
    } else {
      builder.setUserAuthenticationValidityDurationSeconds(authTimeout)
    }
  }

  override fun removeAllKeys() {
    val keyStore: KeyStore
    try {
      keyStore = KeyStore.getInstance("AndroidKeyStore")
      keyStore.load(null)
      val aliases = keyStore.aliases()
      while (aliases.hasMoreElements()) {
        val alias = aliases.nextElement()
        keyStore.deleteEntry(alias)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
