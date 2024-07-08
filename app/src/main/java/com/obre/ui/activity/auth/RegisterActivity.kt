package com.obre.ui.activity.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.obre.R
import com.obre.databinding.ActivityRegisterBinding
import com.obre.ui.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val registerViewModel : RegisterViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etRegisternamapengguna.addTextChangedListener(object  : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                println("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.contains(" ") == true) {
                    binding.etRegisternamapengguna.error = "Spasi tidak diperbolehkan"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                println("Not yet implemented")
            }


        })

        binding.btnDoregister.setOnClickListener {
            val email = binding.etRegisteremail.text.toString().trim()
            val password = binding.etRegisterpassword.text.toString().trim()
            val username = binding.etRegisternamapengguna.text.toString().trim()
            val fullname = binding.etRegisternamalengkap.text.toString().trim()


            if (fullname.isEmpty()) {
                binding.etRegisternamalengkap.error = "Harap isi"
            } else if (username.isEmpty() || username.contains(" ")) {
                binding.etRegisternamapengguna.error = "Harap isi tanpa spasi"
            } else if (email.isEmpty()) {
                binding.etRegisteremail.error = getString(R.string.isi_email)
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etRegisteremail.error = getString(R.string.email_tidak_valid)
            } else if (password.isEmpty()) {
                binding.etRegisterpassword.error = getString(R.string.isi_password)
            } else if (password.length < 8) {
                binding.etRegisterpassword.error = getString(R.string.password_kurang)
            }  else {

                val context = this
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Apakah data sudah sesuai?")
                builder.setPositiveButton(android.R.string.ok) { dialog, which ->

                    showLoading()

                    registerViewModel.register(
                        binding.etRegisteremail.text.toString(),
                        binding.etRegisterpassword.text.toString(),
                        binding.etRegisternamalengkap.text.toString(),
                        binding.etRegisternamapengguna.text.toString()
                    )
                    showToast()

                    registerViewModel.registerResult.observe(this, Observer { result ->
                        result?.let {
                            if (it.isSuccess) {

                                val context = this
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Pemberitahuan")
                                builder.setMessage("Kami sudah mengirimkan pesan ke akun email anda.\nHarap verifikasi akun sebelum melakukan login")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                    finish()
                                }
                                builder.setIcon(android.R.drawable.ic_dialog_alert)
                                builder.show()

                            } else {
                                Toast.makeText(this, "Registrasi gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                }
                    .setNegativeButton("Tidak") { dialog, id ->
                        dialog.cancel()
                    }
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.show()
            }
        }

        binding.registerToLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun showLoading() {
        registerViewModel.isLoading.observe(this@RegisterActivity) {
            binding.pbRegister.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
    private fun showToast() {
        registerViewModel.toastText.observe(this@RegisterActivity) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this@RegisterActivity, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}