package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private fun validateData() {
        //Lay email da nhap
        email = binding.emailEt.text.toString().trim()

        if (email.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập email...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show()
        }
        else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show dialog
        progressDialog.setMessage("Đang gửi yêu cầu đặt lại mật khẩu cho email: $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //Gui thanh cong
                progressDialog.dismiss()
                Toast.makeText(this, "Đã gửi thành công tới email: $email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gửi yêu cầu thất bại!", Toast.LENGTH_SHORT).show()
                print("Error: ${e.message}")
            }
    }

}