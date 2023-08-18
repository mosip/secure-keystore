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
    val ivString = encryptedOutput.take(IV_SIZE)
    val encryptedString = encryptedOutput.drop(IV_SIZE)
    this.iv = Base64.decode(ivString, Base64.DEFAULT)
    this.encryptedData = Base64.decode(encryptedString, Base64.DEFAULT)
  }

  override fun toString(): String {
    return Base64.encodeToString(iv, Base64.DEFAULT) + Base64.encodeToString(encryptedData, Base64.DEFAULT)
  }

  companion object {
    fun validate(encryptedOutput: String): Boolean {
      return encryptedOutput.length > IV_SIZE
    }
  }
}
