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
    val split = encryptedOutput.split(ENCRYPTION_DELIMITER)
    this.iv = Base64.decode(split[0], Base64.DEFAULT)
    this.encryptedData = Base64.decode(split[1], Base64.DEFAULT)
  }

  override fun toString(): String {
    return Base64.encodeToString(iv, Base64.DEFAULT) + ENCRYPTION_DELIMITER + Base64.encodeToString(encryptedData, Base64.DEFAULT)
  }

  companion object {
    const val ENCRYPTION_DELIMITER = "_"
    fun validate(encryptedOutput: String): Boolean {
      return encryptedOutput.split(ENCRYPTION_DELIMITER).size == 2
    }
  }
}
