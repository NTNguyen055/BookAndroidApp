package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

//        Wait
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

//        Ấn nút quay lại
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

//        Thao tác đăng ký
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        // Nhập dữ liệu
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        // Lưu dữ liệu
        if (name.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập tên...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            // email không hợp lệ
            Toast.makeText(this, "Vui lòng nhập Email hợp lệ...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập password...", Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty())
        {
            Toast.makeText(this, "Vui lòng nhập lại password...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword)
        {
            Toast.makeText(this, "Nhập lại password không đúng...", Toast.LENGTH_SHORT).show()
        }
        else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // Xác thực tài khoản mới

        // Cho xem tiến trình
        progressDialog.setMessage("Đang tạo tài khoản...")
        progressDialog.show()

        // Tạo tài khoản trong firebase xác thực
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Tài khoản đã được tạo, thêm vào database
                updateUserInfo()
            }
            .addOnFailureListener {e ->
                // Tạo không thành công
                progressDialog.dismiss()
                Toast.makeText(this, "Tạo tài khoản không thành công vì Email đã tồn tại!", Toast.LENGTH_SHORT).show()
                print("Error: ${e.message}")
            }
    }

    private fun updateUserInfo() {
        // Lưu lại tài khoản - Firebase realtime DB
        progressDialog.setMessage("Đang lưu thông tin người dùng...")

        // Thời gian tạm
        val timestamp = System.currentTimeMillis()

        // Lấy id hiện tại
        val uid = firebaseAuth.uid

        // Thiết lập dữ liệu để lưu lại DB
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // Thiet lap sau
        hashMap["userType"] = "user" // Co the thay doi
        hashMap["timestamp"] = timestamp
        hashMap["password"] = password

        // Đưa dữ liệu vào DB
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                // Tạo thành công, mở dashboard user
                progressDialog.dismiss()
                Toast.makeText(this, "Tài khoản đã được tạo", Toast.LENGTH_SHORT).show()
                startActivity( Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener {e ->
                // Lưu không thành công
                progressDialog.dismiss()
                Toast.makeText(this, "Không lưu được tài khoản!", Toast.LENGTH_SHORT).show()
                print("Error: ${e.message}")
            }
    }
}
