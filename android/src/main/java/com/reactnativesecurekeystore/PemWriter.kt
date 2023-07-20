package com.reactnativesecurekeystore

import android.util.Base64
import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey

class PemWriter {
  companion object {
    private enum class KeyType(val text: String) {
      PUBLIC("PUBLIC KEY"),
      PRIVATE("PRIVATE KEY")
    }

    fun toPemString(publicKey: PublicKey): String {
      return toPemString(publicKey, KeyType.PUBLIC)
    }

    fun toPemString(privateKey: PrivateKey): String {
      return toPemString(privateKey, KeyType.PRIVATE)
    }

    private fun toPemString(key: Key, keyType: KeyType): String {
      val encodedKey = Base64.encodeToString(key.encoded, Base64.DEFAULT)
      val pemStringBuilder = StringBuilder();

      pemStringBuilder.append(preEncapsulationBoundary(key.algorithm, keyType.text))
      pemStringBuilder.appendLine()
      pemStringBuilder.append(encodedKey)
      pemStringBuilder.append(postEncapsulationBoundary(key.algorithm, keyType.text))

      return pemStringBuilder.toString()
    }

    private fun preEncapsulationBoundary(algo: String, keyType: String): String {
      return "-----BEGIN $algo $keyType-----"
    }

    private fun postEncapsulationBoundary(algo: String, keyType: String): String {
      return "-----END $algo $keyType-----"
    }
  }
}
