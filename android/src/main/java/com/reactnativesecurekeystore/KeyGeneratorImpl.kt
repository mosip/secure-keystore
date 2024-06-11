package com.reactnativesecurekeystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import android.util.Log
import com.reactnativesecurekeystore.common.Util
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.security.spec.ECGenParameterSpec

const val KEY_PAIR_KEY_SIZE = 2048

class KeyGeneratorImpl : com.reactnativesecurekeystore.KeyGenerator {
  private val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_TYPE)
  private val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_TYPE)
  private val keyPairGeneratorEC = KeyPairGenerator.getInstance(KEY_ALGORITHM_EC, KEYSTORE_TYPE)
  private val hmacKeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_HMAC_SHA256, KEYSTORE_TYPE)
  private val logTag = Util.getLogTag(javaClass.simpleName)

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?): SecretKey {
    val keySpecBuilder = getKeyGenSpecBuilder(alias)

    if (isAuthRequired) {
      setUserAuth(keySpecBuilder, authTimeout)
    }

    keyGenerator.init(keySpecBuilder.build())

    Log.d(logTag, "generating a new key 123")
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

    override fun generateKeyPairEC(alias: String, isAuthRequired: Boolean, authTimeout: Int?): KeyPair {
    val keySpecBuilder = getKeyPairGenSpecBuilderEC(alias)

    if (isAuthRequired) {
      setUserAuth(keySpecBuilder, authTimeout)
    }

      keyPairGeneratorEC.initialize(keySpecBuilder.build())

    return keyPairGeneratorEC.generateKeyPair()
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
  }

  private fun getKeyPairGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_ENCRYPT or PURPOSE_DECRYPT or PURPOSE_SIGN or PURPOSE_VERIFY

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(KEY_PAIR_KEY_SIZE)
      .setDigests(DIGEST_SHA256, DIGEST_SHA512)
      .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_PKCS1)
      .setSignaturePaddings(SIGNATURE_PADDING_RSA_PKCS1)
  }

  private fun getKeyPairGenSpecBuilderEC(alias: String): KeyGenParameterSpec.Builder {
    val purposes = PURPOSE_ENCRYPT or PURPOSE_DECRYPT or PURPOSE_SIGN or PURPOSE_VERIFY
      return KeyGenParameterSpec.Builder(alias, purposes)
        .setDigests(DIGEST_SHA256,DIGEST_SHA512)
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
      if(authTimeout != 0) {
        builder.setUserAuthenticationParameters(authTimeout, AUTH_BIOMETRIC_STRONG)
      }
    } else {
      val timeout = if(authTimeout == 0) -1 else authTimeout
      builder.setUserAuthenticationValidityDurationSeconds(timeout)
    }
  }
}
