package com.reactnativesecurekeystore

interface SecureKeystore {
  fun hasAlias(alias: String): Boolean
  fun generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): String
  fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?)
  fun removeKey(alias: String)

  fun encryptData(
    alias: String,
    data: String,
    onSuccess: (encryptedText: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  )

  fun decryptData(
    alias: String, encryptedText: String,
    onSuccess: (data: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  )

  fun sign(
    alias: String,
    data: String,
    onSuccess: (signature: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  )

  fun generateHmacSha(
    alias: String, data: String,
    onSuccess: (signature: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  )
   fun generateHmacSha256Key(alias: String)
}
