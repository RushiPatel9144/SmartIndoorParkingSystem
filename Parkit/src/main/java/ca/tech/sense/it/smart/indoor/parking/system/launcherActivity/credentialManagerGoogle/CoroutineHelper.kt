package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle

import android.content.Context
import android.widget.Toast
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoroutineHelper {
    companion object {
        fun signInWithGoogle(
            context: Context,
            googleAuthClient: GoogleAuthClient,
            onSuccess: Runnable
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val success = googleAuthClient.signIn()
                    val message = if (success) "Sign-in successful" else "Goooooogle Sign-in failed"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) run {
                        onSuccess.run()
                    }
                } catch (e: NoCredentialException) {
                    Toast.makeText(
                        context,
                        "No credentials available. Please try another sign-in method.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Error during sign-in: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
