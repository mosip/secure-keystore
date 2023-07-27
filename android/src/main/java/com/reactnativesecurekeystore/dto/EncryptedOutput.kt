package com.reactnativesecurekeystore.dto

import android.util.Base64

class EncryptedOutput {
  var iv: ByteArray
  var encryptedData: ByteArray

  constructor(encryptedData: ByteArray, iv: ByteArray) {
    this.encryptedData = encryptedData
    this.iv = iv
  }

  constructor(encryptedOutput: String) {
    val split = encryptedOutput.split("_")
    this.iv = Base64.decode(split[0], Base64.DEFAULT)
    this.encryptedData = Base64.decode(split[1], Base64.DEFAULT)
  }

  override fun toString(): String {
    return Base64.encodeToString(iv, Base64.DEFAULT) + '_' + Base64.encodeToString(encryptedData, Base64.DEFAULT)
  }

  companion object {
    fun validate(encryptedOutput: String): Boolean {
      return encryptedOutput.split("_").size > 2
    }
  }
}
