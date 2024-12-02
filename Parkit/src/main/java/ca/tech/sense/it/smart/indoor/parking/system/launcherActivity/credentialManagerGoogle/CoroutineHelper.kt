package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle

import android.content.Context
import android.widget.Toast
import androidx.credentials.exceptions.NoCredentialException
import ca.tech.sense.it.smart.indoor.parking.system.R
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
                    val message = if (success) context.getString(R.string.sign_in_successful) else context.getString(R.string.google_sign_in_failed)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) run {
                        onSuccess.run()
                    }
                } catch (e: NoCredentialException) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_credentials_available_please_try_another_sign_in_method),
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

        fun signOutWithGoogle(
            context: Context,
            googleAuthClient: GoogleAuthClient,
            onSuccess: Runnable
        ) { CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = googleAuthClient.signOut()
                val message = if (success) {
                    context.getString(R.string.sign_out_successful) } else context.getString(R.string.google_sign_out_failed)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (success) run {
                    onSuccess.run()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error during sign-out: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        }
    }
}
