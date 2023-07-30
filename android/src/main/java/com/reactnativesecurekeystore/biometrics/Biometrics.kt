package com.reactnativesecurekeystore.biometrics

import BiometricPromptAuthCallback
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.UiThreadUtil
import com.reactnativesecurekeystore.common.util
import java.security.Signature
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.P)
class Biometrics(
  private val context: Context) {
  private val logTag = util.getLogTag(javaClass.simpleName)

  enum class ErrorCode {
    INTERNAL_ERROR,
    CANCELLED_BY_USER,
  }

  fun authenticate(
    signature: Signature,
    onSuccess: (cryptoObject: BiometricPrompt.CryptoObject) -> Unit,
    onFailure: (errorCode: ErrorCode, errString: String) -> Unit
  ) {
      UiThreadUtil.runOnUiThread {
        try {
          val authCallback: BiometricPrompt.AuthenticationCallback = BiometricPromptAuthCallback(onSuccess, onFailure)
          val executor: Executor = Executors.newSingleThreadExecutor()
          val cancellationSignal = CancellationSignal()
          val biometricPrompt = buildBiometricPrompt(executor, onFailure)
          val cryptoObject = BiometricPrompt.CryptoObject(signature)

          biometricPrompt.authenticate(
            cryptoObject,
            cancellationSignal,
            executor,
            authCallback,
          )
        } catch (e: Exception) {
          Log.e(logTag, "exception in creating auth prompt: ", e)
          onFailure(ErrorCode.INTERNAL_ERROR, "Failed to display auth prompt")
        }
      }
  }

  private fun buildBiometricPrompt(executor: Executor, onFailure: (errorCode: ErrorCode, errString: String) -> Unit): BiometricPrompt {
    val builder = BiometricPrompt.Builder(context)
      .setTitle("Unlock App")
      .setDescription("Enter phone screen lock pattern, PIN< password or fingerprint")
      .setNegativeButton("Cancel", executor) { _, _ ->
        onFailure(ErrorCode.CANCELLED_BY_USER, "Cancelled by clicking on negative button")
      }

    return builder.build()
  }
}
