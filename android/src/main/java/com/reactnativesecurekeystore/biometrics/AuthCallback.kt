import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricPrompt.CryptoObject
import android.os.Build
import androidx.annotation.RequiresApi
import com.reactnativesecurekeystore.biometrics.Biometrics


@RequiresApi(Build.VERSION_CODES.P)
class BiometricPromptAuthCallback (
  val onSuccess: (cryptoObject: CryptoObject) -> Unit,
  val onFailure: (errorCode: Biometrics.ErrorCode, errString: String) -> Unit
) : BiometricPrompt.AuthenticationCallback() {
  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)

    onFailure(Biometrics.ErrorCode.INTERNAL_ERROR, "$errString, Internal Error Code:  $errorCode")
  }

  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)

    onSuccess(result.cryptoObject)
  }
}
