import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import com.reactnativesecurekeystore.common.Util
import com.reactnativesecurekeystore.exception.KeyInvalidatedException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.acos

class BiometricPromptAuthCallback(
  private val continuation: Continuation<Unit>,
  private val action: (CryptoObject?) -> Unit,
) : BiometricPrompt.AuthenticationCallback() {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)

    continuation.resumeWithException(RuntimeException("User has cancelled biometric auth code: $errorCode, message: $errString"))
  }

  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)
    val cryptoObject = result.cryptoObject

    try {
      action(cryptoObject)
      continuation.resume(Unit)
    } catch (e: Exception) {
      // If Auth is timeout and key is invalidated, we get user not Auth exception even after auth
      when (e) {
        is UserNotAuthenticatedException, is KeyPermanentlyInvalidatedException -> {
          Log.e(logTag, "Exception in init after biometric auth, this happens if key is invalidated in timeout based auth", e)
          continuation.resumeWithException(KeyInvalidatedException())
        }
        else -> {
          Log.e(logTag, "Exception in action after biometric auth", e)
          continuation.resumeWithException(RuntimeException("Action Failed after biometric auth success"))
        }
      }

    }
  }
}
