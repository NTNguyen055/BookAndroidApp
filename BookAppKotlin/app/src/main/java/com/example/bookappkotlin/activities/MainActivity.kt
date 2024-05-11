package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Xử lý click, đăng nhập
        binding.loginBtn.setOnClickListener {
            startActivity( Intent(this, LoginActivity::class.java))
        }

        //Xử lý click, tiếp tục
        binding.skipBtn.setOnClickListener {
            startActivity( Intent(this, DashboardUserActivity::class.java))
        }


    }
}
