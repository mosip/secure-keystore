package com.reactnativesecurekeystore.biometrics

import BiometricPromptAuthCallback
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.reactnativesecurekeystore.common.util
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.IllegalBlockSizeException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Biometrics(
  private val context: ReactApplicationContext
) {
  private val logTag = util.getLogTag(javaClass.simpleName)

  enum class ErrorCode {
    INTERNAL_ERROR,
  }

  suspend fun authenticateAndPerform(
    preAction: () -> BiometricPrompt.CryptoObject,
    action: (BiometricPrompt.CryptoObject) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    val preAuthCryptoObject = preAction()

    try {
      Log.d(logTag, "Calling action for auth")
      action(preAuthCryptoObject)
    } catch (e: IllegalBlockSizeException) {
      Log.e(logTag, "Calling action failed due to auth exception", e)
      authenticate(preAction, action)
    } catch (e: Exception) {
      Log.e(logTag, "Calling action failed due to other exception", e)
      onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
    }
  }

  private suspend fun authenticate(
    preAction: () -> BiometricPrompt.CryptoObject,
    action: (BiometricPrompt.CryptoObject) -> Unit
  ) {

    return suspendCoroutine { continuation ->
      UiThreadUtil.runOnUiThread {
        try {
          val cryptoObject = preAction()
          val fragmentActivity = context.currentActivity ?: throw NullPointerException("Not assigned current activity")
          val authCallback: BiometricPrompt.AuthenticationCallback = BiometricPromptAuthCallback(continuation, action)
          val executor: Executor = Executors.newSingleThreadExecutor()

          val promptInfo = createPromptInfo()
          val biometricPrompt = BiometricPrompt(fragmentActivity as FragmentActivity, executor, authCallback)

          biometricPrompt.authenticate(promptInfo, cryptoObject)

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
