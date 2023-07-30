import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.UiThreadUtil
import java.security.Signature
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.P)
class Biometrics(
  private val context: Context) {

  fun authenticate(
    signature: Signature,
    onSuccess: (cryptoObject: BiometricPrompt.CryptoObject) -> Unit,
    onFailure: (errorCode: Int, errString: String) -> Unit
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
          onFailure(0, "Failed to display auth prompt")
        }
      }
  }

  private fun buildBiometricPrompt(executor: Executor, onFailure: (errorCode: Int, errString: String) -> Unit) = BiometricPrompt.Builder(context)
    .setTitle("Unlock Inji")
    .setDescription("Enter phone screen lock pattern, PIN< password or fingerprint")
    .setNegativeButton("Cancel", executor) { _, _ ->
      onFailure(1, "Cancelled by clicking on negative button")
    }
    .build()
}
