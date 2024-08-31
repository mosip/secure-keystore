package com.reactnativesecurekeystore

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricPrompt.CryptoObject
import com.reactnativesecurekeystore.biometrics.Biometrics
import com.reactnativesecurekeystore.common.PemConverter
import com.reactnativesecurekeystore.common.Util.Companion.getLogTag
import com.reactnativesecurekeystore.dto.EncryptedOutput
import com.reactnativesecurekeystore.exception.InvalidEncryptionText
import com.reactnativesecurekeystore.exception.KeyNotFound
import kotlinx.coroutines.runBlocking
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey


class SecureKeystoreImpl(
    private val keyGenerator: KeyGenerator,
    private val cipherBox: CipherBox,
    private val biometrics: Biometrics,
    private val preferences: Preferences,
) : SecureKeystore {
    private var ks: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
    private val logTag = getLogTag(javaClass.simpleName)

    init {
        ks.load(null)
    }

    /** Generate secret key and store it in AndroidKeystore */
    override fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?) {
        keyGenerator.generateKey(alias, isAuthRequired, authTimeout)
    }

    /** Generate a new key pair */
    override fun generateKeyPair(
        type: String,
        alias: String,
        isAuthRequired: Boolean,
        authTimeout: Int?,
    ): String {
        val keyPair: KeyPair
        keyPair = if (type == "RS256") {
            keyGenerator.generateKeyPair(alias, isAuthRequired, authTimeout)
        } else if (type=="ES256")
            keyGenerator.generateKeyPairEC(alias, isAuthRequired, authTimeout)
        else
            throw KeyNotFound("Given key type $type is not supported")
        return PemConverter(keyPair.public).toPem()
    }

    override fun retrieveKey(
        alias: String,
    ): String {
        val publicKey = ks.getCertificate(alias).publicKey
        return PemConverter(publicKey).toPem()
    }

    fun generateKeyPairEC(
        alias: String,
        isAuthRequired: Boolean,
        authTimeout: Int?,
    ): String {
        val keyPair = keyGenerator.generateKeyPairEC(alias, isAuthRequired, authTimeout)
        Log.d("keytest", PemConverter(keyPair.public).toPem())
        return PemConverter(keyPair.public).toPem()
    }

    override fun generateHmacSha256Key(alias: String) {
        keyGenerator.generateHmacKey(alias)
    }

    /** Remove key with provided name from security storage.  */
    override fun removeKey(alias: String) {
        ks.load(null)

        if (ks.containsAlias(alias)) {
            ks.deleteEntry(alias)
        }
    }

    override fun hasAlias(alias: String): Boolean {
        ks.load(null)
        return ks.containsAlias(alias)
    }

    override fun encryptData(
        alias: String,
        data: String,
        onSuccess: (encryptedText: String) -> Unit,
        onFailure: (code: Int, message: String) -> Unit,
        context: Context,
    ) {
        try {
            val key = getKeyOrThrow(alias)

            runBlocking {
                val createCryptoObject = {
                    CryptoObject(cipherBox.initEncryptCipher(key))
                }

                val action = { cryptoObject: CryptoObject ->
                    val encryptedText =
                        cipherBox.encryptData(cryptoObject.cipher!!, data).toString()
                    onSuccess(encryptedText)
                }

                biometrics.authenticateAndPerform(createCryptoObject, action, onFailure, context)
            }
        } catch (ex: Exception) {
            onFailure(ex.hashCode(), ex.message.toString())
        }
    }

    override fun decryptData(
        alias: String, encryptedText: String,
        onSuccess: (data: String) -> Unit,
        onFailure: (code: Int, message: String) -> Unit,
        context: Context,
    ) {
        try {
            val key = getKeyOrThrow(alias)

            if (!EncryptedOutput.validate(encryptedText)) {
                throw InvalidEncryptionText()
            }

            runBlocking {
                val encryptedOutput = EncryptedOutput(encryptedText)

                val createCryptoObject =
                    { CryptoObject(cipherBox.initDecryptCipher(key, encryptedOutput)) }

                val action = { cryptoObject: CryptoObject ->
                    val data = cipherBox.decryptData(cryptoObject.cipher!!, encryptedOutput)
                    onSuccess(String(data))
                }

                biometrics.authenticateAndPerform(createCryptoObject, action, onFailure, context)
            }
        } catch (ex: Exception) {
            onFailure(ex.hashCode(), ex.message.toString())
        }
    }

    override fun sign(
        signAlgorithm: String,
        alias: String, data: String,
        onSuccess: (signature: String) -> Unit, onFailure: (code: Int, message: String) -> Unit,
        context: Context,
    ) {
        try {
            val key = getKeyOrThrow(alias) as PrivateKey

            runBlocking {
                val createCryptoObject =
                    { CryptoObject(cipherBox.createSignature(key, signAlgorithm)) }

                val action = { cryptoObject: CryptoObject ->
                    val signatureText =
                        cipherBox.sign(cryptoObject.signature!!, data, signAlgorithm)
                    onSuccess(signatureText)
                }
                biometrics?.authenticateAndPerform(
                    createCryptoObject,
                    action,
                    onFailure,
                    context
                )
            }
        } catch (e: RuntimeException) {
            Log.e(logTag, "Exception in sign creation: ", e)
            onFailure(e.hashCode(), e.message.toString())
        }
    }

    override fun generateHmacSha(
        alias: String, data: String,
        onSuccess: (sha: String) -> Unit,
        onFailure: (code: Int, message: String) -> Unit,
    ) {
        try {
            val key = getKeyOrThrow(alias) as SecretKey
            val hmacSha: ByteArray = cipherBox.generateHmacSha(key, data)
            onSuccess(String(hmacSha))
        } catch (e: RuntimeException) {
            Log.e(logTag, "Exception in Hmac generation: ", e)
            onFailure(e.hashCode(), e.message.toString())
        }
    }

    public fun getKeyOrThrow(alias: String): Key {
        ks.load(null)
        if (!ks.containsAlias(alias)) {
            throw KeyNotFound("Key not found for the alias: $alias")
        }
        return ks.getKey(alias, null)
    }

    override fun removeAllKeys() {
        try {
            ks.load(null)
            val aliases = ks.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                ks.deleteEntry(alias)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun retrieveGenericKey(account: String): List<String> {
        val privateKey= preferences.getPreference("${account}_privateKey", "")
        val publicKey= preferences.getPreference("${account}_public_Key", "")
        val keyPair=ArrayList<String>()
        keyPair.add(privateKey)
        keyPair.add(publicKey)
        return keyPair
    }

    override fun storeGenericKey(
        privateKey: String,
        publicKey: String,
        account: String,
    ) {
        preferences.savePreference("${account}_privateKey",privateKey)
        preferences.savePreference("${account}_publicKey",publicKey)
    }
}
