package com.project.farmingapp.view.auth


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.project.farmingapp.R
import com.project.farmingapp.databinding.ActivityPhoneAuthBinding
import com.project.farmingapp.view.dashboard.DashboardActivity
import com.project.farmingapp.view.introscreen.IntroActivity
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var verificationId: String

    private var firstTime: Boolean? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnGenerateOTP.setOnClickListener {
            verifyPhoneNumber()
        }

        firstTimeRun()
    }

    private fun firstTimeRun() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        firstTime = sharedPreferences.getBoolean("firstTime", true)

        if (firstTime!!) {
            Intent(this, IntroActivity::class.java).also {
                startActivity(it)
            }
            finish()
            return
        }
    }

    private fun verifyPhoneNumber() {
        binding.progressBar.visibility = View.VISIBLE

        val countryCode = binding.editTextCountryCode.text.toString().trim()
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()

        val fullPhoneNumber = countryCode + phoneNumber

        if (fullPhoneNumber.isNotEmpty()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                fullPhoneNumber,
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        showToast("Verification failed: ${e.message}")
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        this@PhoneAuthActivity.verificationId = verificationId

                        binding.progressBar.visibility = View.GONE

                        val intent = Intent(this@PhoneAuthActivity, OtpAuthActivity::class.java)
                        intent.putExtra("verificationId", verificationId)
                        startActivity(intent)
                        finish()
                    }
                }
            )
        } else {
            showToast("Please enter a valid phone number")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    showToast("Verification successful! User: ${user?.phoneNumber}")
                } else {
                    val message = task.exception?.message ?: "Unknown error occurred"
                    showToast("Verification failed: $message")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
