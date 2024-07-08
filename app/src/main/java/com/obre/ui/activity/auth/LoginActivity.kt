package com.obre.ui.activity.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.obre.R
import com.obre.databinding.ActivityLoginBinding
import com.obre.ui.activity.MainActivity
import com.obre.ui.activity.OnBoardingActivity
import com.obre.ui.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loginViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(LoginViewModel::class.java)

        if (loginViewModel.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        Toast.makeText(
            this,
            getString(R.string.selamat_datang),
            Toast.LENGTH_SHORT
        ).show()

        loginViewModel.loginResult.observe(this, { loginResult ->
            if (loginResult) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        val sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()
            startActivity(Intent(this, OnBoardingActivity::class.java))
            finish()
            return
        }

        binding.btnDologin.setOnClickListener {
            val email = binding.etLoginemail.text.toString().trim()
            val password = binding.etLoginpassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etLoginemail.error = getString(R.string.isi_email)
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etLoginemail.error = getString(R.string.email_tidak_valid)
            } else if (password.isEmpty()) {
                binding.etLoginpassword.error = getString(R.string.isi_password)
            } else if (password.length < 8) {
                binding.etLoginpassword.error = getString(R.string.password_kurang)
            } else {
                showLoading()
                loginViewModel.login(email, password) { isVerified, errorMessage ->
                    hideLoading()
                    if (isVerified) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        binding.btnGoregisterfromlogin.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            showResetDialog()
        }
    }

    private fun showResetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_reset_password, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailReset)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Masukkan alamat email:")
            .setView(dialogView)
            .setPositiveButton("Reset", null)
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val email = emailEditText?.text?.toString()?.trim()
                if (email.isNullOrEmpty()) {
                    emailEditText?.error = "Harap masukkan alamat email"
                } else {
                    binding.pbLogin.visibility = View.VISIBLE
                    resetPassword(email)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.pbLogin.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Password reset sudah dikirim ke email $email",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Gagal mengirim email reset password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showLoading() {
        loginViewModel.isLoading.observe(this@LoginActivity) { isLoading ->
            binding.pbLogin.visibility = if (isLoading == true) View.VISIBLE else View.GONE
        }
    }

    private fun hideLoading() {
        binding.pbLogin.visibility = View.GONE
    }
}