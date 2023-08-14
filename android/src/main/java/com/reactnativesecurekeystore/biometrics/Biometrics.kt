package com.reactnativesecurekeystore.biometrics

import BiometricPromptAuthCallback
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.reactnativesecurekeystore.common.Util
import com.reactnativesecurekeystore.exception.ErrorCode
import java.security.Key
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.IllegalBlockSizeException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Biometrics(
  private val context: ReactApplicationContext
) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  suspend fun authenticateAndPerform(
    createCryptoObject: () -> CryptoObject,
    action: (CryptoObject) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    try {
      Log.d(logTag, "Calling action for auth")
      val preAuthCryptoObject = createCryptoObject()
      action(preAuthCryptoObject)
    } catch (e: UserNotAuthenticatedException) {
      // If key has timeout based biometric auth requirement, Cipher.Init fails and caught here
      Log.e(logTag, "Calling action failed due to auth exception with user not auth", e)
      authenticate(createCryptoObject, action, false)
    }
    catch (e: IllegalBlockSizeException) {
      // If key has every use biometric auth requirement, Cipher.doFinal fails and caught here
      Log.e(logTag, "Calling action failed due to auth exception", e)
      authenticate(createCryptoObject, action, true)
    } catch (e: Exception) {
      Log.e(logTag, "Calling action failed due to other exception", e)
      onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
    }
  }

  private suspend fun authenticate(
    createCryptoObject: () -> CryptoObject,
    action: (CryptoObject) -> Unit,
    createCryptoObjectSuccess: Boolean
  ) {

    return suspendCoroutine { continuation ->
      UiThreadUtil.runOnUiThread {
        try {
          val fragmentActivity = context.currentActivity ?: throw NullPointerException("Not assigned current activity")
          val onAuthSuccess:(CryptoObject?) -> Unit = { cryptoObject -> action(cryptoObject ?: createCryptoObject()) }
          val authCallback: BiometricPrompt.AuthenticationCallback = BiometricPromptAuthCallback(continuation, onAuthSuccess)
          val executor: Executor = Executors.newSingleThreadExecutor()

          val promptInfo = createPromptInfo()
          val biometricPrompt = BiometricPrompt(fragmentActivity as FragmentActivity, executor, authCallback)

          if(createCryptoObjectSuccess) {
            biometricPrompt.authenticate(promptInfo, createCryptoObject())
          } else {
            biometricPrompt.authenticate(promptInfo)
          }

        } catch (e: Exception) {
          Log.e(logTag, "exception in creating auth prompt: ", e)
          continuation.resumeWithException(RuntimeException("exception in creating auth prompt"))
        }
      }
    }
  }

  private fun createPromptInfo(): PromptInfo {
    return PromptInfo.Builder()
      .setTitle("Unlock App")
      .setDescription("Enter phone screen lock pattern, PIN, password or fingerprint")
      .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
      .build()
  }
}
