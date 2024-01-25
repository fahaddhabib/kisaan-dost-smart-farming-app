package com.project.farmingapp.view.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.project.farmingapp.databinding.ActivityOtpAuthBinding
import com.project.farmingapp.utilities.checkUserExists
import com.project.farmingapp.view.dashboard.DashboardActivity

class OtpAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpAuthBinding

    private lateinit var verificationId: String
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = intent.getStringExtra("verificationId") ?: ""
        firebaseAuth = FirebaseAuth.getInstance()

        requestFocusOTP()

        binding.btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }
    }

    private fun requestFocusOTP() {
        val otpFields = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        for (i in 0 until otpFields.size - 1) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Not used
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Check if the current EditText has reached the desired length
                    if (s?.length == 1) {
                        // Move the focus to the next EditText field
                        otpFields[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Not used
                }
            })
        }
    }

    private fun verifyOtp() {
        binding.progressBar.visibility = View.VISIBLE

        val otpCode = StringBuilder()
        otpCode.append(binding.etOtp1.text.toString())
        otpCode.append(binding.etOtp2.text.toString())
        otpCode.append(binding.etOtp3.text.toString())
        otpCode.append(binding.etOtp4.text.toString())
        otpCode.append(binding.etOtp5.text.toString())
        otpCode.append(binding.etOtp6.text.toString())

        val concatenatedOtpCode = otpCode.toString()

        if (concatenatedOtpCode.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(verificationId, concatenatedOtpCode)
            signInWithPhoneAuthCredential(credential)
        } else {
            showToast("Please enter the OTP")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Phone number verification successful
                    val user = task.result?.user

                    // After successful OTP verification
                    val phoneNumber = "${user?.phoneNumber}" // Replace with the user's actual phone number
                    checkUserExists(phoneNumber) { exists ->
                        if (exists) {
                            // User already registered
                            binding.progressBar.visibility = View.GONE
                            showToast("User Already Registered: ${user?.phoneNumber}")

                            // Proceed to the next screen/activity
                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (!exists) {
                            // User not registered
                            binding.progressBar.visibility = View.GONE
                            showToast("Verification successful! User: ${user?.phoneNumber}")

                            // Proceed to the next screen/activity
                            val intent = Intent(this, UserSignUpForm::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // Phone number verification failed
                    binding.progressBar.visibility = View.GONE
                    val message = task.exception?.message ?: "Unknown error occurred"
                    showToast("Verification failed: $message")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
