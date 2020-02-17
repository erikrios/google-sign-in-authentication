package com.erikriosetiawan.googlesigninauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.erikriosetiawan.googlesigninauthentication.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    // Adding Google sign-in client
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // Creating member variable for FirebaseAuth
    private lateinit var mAuth: FirebaseAuth

    companion object {
        // Adding tag for logging and RC_SIGN_ID for an activity result
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setButtonListener()

        // Building Google sign-in and sign-up option.
        // Configuring Google Sign IN
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by googleSignInOptions
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Set the dimensions of the sign-in button
        binding.buttonSignInWithGoogle.setSize(SignInButton.SIZE_WIDE)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Checking if the user is signed in (non-null) and update UI accordingly
        val currentUser: FirebaseUser? = mAuth.currentUser

        if (currentUser != null) {
            Log.d(TAG, "Currently Signed in: ${currentUser.email}")
            Toast.makeText(
                this, "Currently Logged in: ${currentUser.email}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Calling onActivityResult to use the information
    // about the sign-in user contains in the object.

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogeSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                Toast.makeText(
                    this,
                    "Google Sign In Succeeded", Toast.LENGTH_LONG
                ).show()
                firebaseAuthwithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update IU appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(
                    this, "Google Sign in Failed $e",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Creating helper method FirebaseAuthWithGoogle()
    private fun firebaseAuthwithGoogle(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account?.id}")

        // Calling get credential from the googleAuthProvider
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) {
                if (it.isSuccessful) {
                    // Update UI with the sign-in user's information
                    val user: FirebaseUser? = mAuth.currentUser
                    Log.d(TAG, "signInWithCredential:success: currentUser: ${user?.email}")
                    Toast.makeText(
                        this,
                        "Firebase Authentication Succeeded", Toast.LENGTH_LONG
                    ).show()
                } else {
                    // If sign-in fails to display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", it.exception)
                    Toast.makeText(
                        this, "Firebase Authentication Failed ${it.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun signInToGoogle() {
        // Calling Intent and call startActivityForResult() method
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        //  Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(
                applicationContext, "Signed out of Google",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Revoked Access")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_sign_in_with_google -> {
                signInToGoogle()
            }
            R.id.button_sign_out -> {
                signOut()
            }
            R.id.button_sign_in_and_disconnect -> {
                revokeAccess()
            }
        }
    }

    private fun setButtonListener() {
        binding.buttonSignInWithGoogle.setOnClickListener(this)
        binding.buttonSignOut.setOnClickListener(this)
        binding.buttonSignInAndDisconnect.setOnClickListener(this)
    }
}
