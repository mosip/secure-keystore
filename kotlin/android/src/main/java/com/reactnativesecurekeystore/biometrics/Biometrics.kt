package com.reactnativesecurekeystore.biometrics

import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import com.reactnativesecurekeystore.common.Util
import com.reactnativesecurekeystore.exception.ErrorCode
import com.reactnativesecurekeystore.exception.KeyInvalidatedException
import java.security.SignatureException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.IllegalBlockSizeException
import android.content.Context
import java.lang.Exception
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Biometrics(
) {
    private val logTag = Util.getLogTag(javaClass.simpleName)

    companion object {
        var POPUP_TITLE = "Unlock App"
        var POPUP_DESCRIPTION = "Please use fingerprint to unlock the app"

        fun updatePopupDetails(title: String, description: String) {
            POPUP_TITLE = title
            POPUP_DESCRIPTION = description
        }
    }

    suspend fun authenticateAndPerform(
        createCryptoObject: () -> CryptoObject,
        action: (CryptoObject) -> Unit,
        onFailure: (code: Int, message: String) -> Unit,
        context: Context,
    ) {
        try {
            val preAuthCryptoObject = createCryptoObject()
            action(preAuthCryptoObject)
        } catch (e: UserNotAuthenticatedException) {
            // If key has timeout based biometric auth requirement, Cipher.Init fails and caught here
            Log.e(logTag, "Calling action failed due to auth exception with user not auth", e)
            authenticate(createCryptoObject, action, false, context)
        } catch (e: Exception) {
            // If key has every use biometric auth requirement, Cipher.doFinal fails and caught here
            when (e) {
                is IllegalBlockSizeException, is SignatureException -> {
                    Log.e(logTag, "Calling action failed due to auth exception", e)
                    authenticate(createCryptoObject, action, true, context)
                }

                is KeyPermanentlyInvalidatedException -> {
                    throw KeyInvalidatedException()
                }

                else -> throw e
            }
        } catch (e: Exception) {
            Log.e(logTag, "Calling action failed due to other exception", e)
            onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
        }
    }

    fun isBiometricEnabled(
        context: Context,
    ): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private suspend fun authenticate(
        createCryptoObject: () -> CryptoObject,
        action: (CryptoObject) -> Unit,
        createCryptoObjectSuccess: Boolean,
        context: Context,
    ) {
        return suspendCoroutine { continuation ->
            val fragmentActivity = context as? FragmentActivity
                ?: throw NullPointerException("Context is not a FragmentActivity")
            fragmentActivity.runOnUiThread {
                try {
                    val onAuthSuccess: (CryptoObject?) -> Unit =
                        { cryptoObject -> action(cryptoObject ?: createCryptoObject()) }
                    val authCallback: BiometricPrompt.AuthenticationCallback =
                        BiometricPromptAuthCallback(continuation, onAuthSuccess)
                    val executor: Executor = Executors.newSingleThreadExecutor()

                    val promptInfo = createPromptInfo()
                    val biometricPrompt =
                        BiometricPrompt(context as FragmentActivity, executor, authCallback)

                    if (createCryptoObjectSuccess) {
                        biometricPrompt.authenticate(promptInfo, createCryptoObject())
                    } else {
                        try {
                            biometricPrompt.authenticate(promptInfo)
                        } catch (e: Exception) {
                            Log.e(logTag, e.toString())
                        }

                    }

                } catch (e: Exception) {
                    Log.e(logTag, "exception in creating auth prompt: ", e)
                    continuation.resumeWithException(RuntimeException("exception in creating auth prompt"))
                }
            }
        }
    }

    private fun createPromptInfo(): PromptInfo {
        val builder = PromptInfo.Builder().setTitle(POPUP_TITLE).setDescription(POPUP_DESCRIPTION)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setNegativeButtonText("Cancel")
        }

        return builder.build()
    }
}
