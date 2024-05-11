package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

//        Wait
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

//        Nhan khong co tai khoan
        binding.noAccountTv.setOnClickListener {
            startActivity( Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        // Nhap email va password
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // Kiem tra du lieu nhap vao
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show()
        }
        else if (email.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập email...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập mật khẩu...", Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun loginUser() {
        // Kiem tra dang nhap

        progressDialog.setMessage("Đang đăng nhập...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show()
                println("Error: ${e.message}")
            }
    }

    private fun checkUser() {
        // Kiem tra loai dang nhap - FBAuth
        progressDialog.setMessage("Đang kiểm tra...")

        val firebaseUser = firebaseAuth.currentUser!!
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange (snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    // Lấy kiểu người đăng nhập user or admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user")
                    {
                        startActivity( Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin")
                    {
                        startActivity( Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
