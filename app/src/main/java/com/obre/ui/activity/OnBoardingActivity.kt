package com.obre.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.obre.databinding.ActivityWelcomeBinding
import com.obre.ui.activity.auth.LoginActivity
import com.obre.ui.activity.auth.RegisterActivity

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGologin.setOnClickListener {
            startActivity(Intent(this@OnBoardingActivity, LoginActivity::class.java))
        }

        binding.btnGoregister.setOnClickListener {
            startActivity(Intent(this@OnBoardingActivity, RegisterActivity::class.java))
        }
    }
}
