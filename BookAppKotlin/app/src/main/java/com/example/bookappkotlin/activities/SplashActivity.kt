package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        //Tạo 2s delay r vào ứng dụng
        Handler().postDelayed(Runnable {
            checkUser()
        }, 2000)
    }

    private fun checkUser() {
        // Lay gia tri hien tai
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null)
        {
            startActivity( Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange (snapshot: DataSnapshot) {

                        // Lấy kiểu người đăng nhập user or admin
                        val userType = snapshot.child("userType").value
                        if (userType == "user")
                        {
                            startActivity( Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        }
                        else if (userType == "admin")
                        {
                            startActivity( Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}