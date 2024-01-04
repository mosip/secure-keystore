package com.reactnativesecurekeystore.dto

import android.util.Base64

const val IV_SIZE = 16

class EncryptedOutput {
  var iv: ByteArray
  var encryptedData: ByteArray

  constructor(encryptedData: ByteArray, iv: ByteArray) {
    this.encryptedData = encryptedData
    this.iv = iv
  }

  constructor(encryptedOutput: String) {
    val b = Base64.decode(encryptedOutput, Base64.DEFAULT)
    this.iv = b.take(IV_SIZE).toByteArray()
    this.encryptedData = b.drop(IV_SIZE).toByteArray()
  }

  override fun toString(): String {
    return Base64.encodeToString(iv.plus(encryptedData), Base64.DEFAULT)
  }

  companion object {
    fun validate(encryptedOutput: String): Boolean {
      return encryptedOutput.length > IV_SIZE
    }
  }
}
