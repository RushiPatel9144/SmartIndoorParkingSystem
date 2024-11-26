    package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity.credentialManagerGoogle

    import android.content.Context
    import android.util.Log
    import androidx.credentials.ClearCredentialStateRequest
    import androidx.credentials.CredentialManager
    import androidx.credentials.CustomCredential
    import androidx.credentials.GetCredentialRequest
    import androidx.credentials.GetCredentialResponse
    import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton
    import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton
    import ca.tech.sense.it.smart.indoor.parking.system.model.user.User
    import ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.APP_CLIENT_ID
    import com.google.android.libraries.identity.googleid.GetGoogleIdOption
    import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
    import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
    import com.google.firebase.auth.GoogleAuthProvider
    import kotlinx.coroutines.tasks.await
    import kotlin.coroutines.cancellation.CancellationException


    class GoogleAuthClient(
        private val context: Context,
    ) {
        private val tag = "GoogleAuthClient: "

        private val credentialManager = CredentialManager.create(context)
        private val firebaseAuth = FirebaseAuthSingleton.getInstance()
        private val fireStore = FirestoreSingleton.getInstance()

        fun isSingedIn(): Boolean {
            if (firebaseAuth.currentUser != null) {
                println(tag + "already signed in")
                return true
            }

            return false
        }


        suspend fun signIn(): Boolean {
            if (isSingedIn()) {
                return true
            }

            try {
                val result = buildCredentialRequest()
                return handleSignIn(result)
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e

                Log.e(tag, "Sign-in error: ${e.message}")
                return false
            }
        }

        private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                    // Check if the user already exists in Firestore by their ID token
                    val userDocumentRef = fireStore.collection("users").document(authResult.user?.uid ?: tokenCredential.idToken)
                    val documentSnapshot = userDocumentRef.get().await()

                    if (!documentSnapshot.exists()) {
                        // If user does not exist, create a new user in Firestore
                        val localUser = User(
                            /* uid = */ authResult.user?.uid,
                            /* firstName = */ tokenCredential.displayName,
                            /* lastName = */ "",
                            /* email = */ tokenCredential.id,
                            /* phone = */ tokenCredential.phoneNumber,
                            /* profilePhotoUrl = */ tokenCredential.profilePictureUri.toString()
                        )

                        // Save the user data to Firestore
                        userDocumentRef.set(localUser)
                            .addOnSuccessListener {
                                Log.d(tag, "User profile created for ${tokenCredential.idToken}")
                            }
                            .addOnFailureListener { e ->
                                Log.e(tag, "Error saving user data: ${e.message}")
                            }
                    } else {
                        Log.d(tag, "User profile already exists for ${tokenCredential.idToken}")
                    }

                    return authResult.user != null
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(tag, "GoogleIdTokenParsingException: ${e.message}")
                    return false
                }
            } else {
                Log.e(tag, "Credential is not GoogleIdTokenCredential")
                return false
            }
        }

        private suspend fun buildCredentialRequest(): GetCredentialResponse {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(APP_CLIENT_ID)
                        .setAutoSelectEnabled(false)
                        .build()
                )
                .build()

            return credentialManager.getCredential(
                request = request, context = context
            )
        }

        suspend fun signOut(): Boolean {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()

            return true
        }

    }