package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCategoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập tên sách...", Toast.LENGTH_SHORT).show()
        }
        else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        // Tao cac cot du lieu trong fbdb
        val hashmap = HashMap<String, Any>()
        hashmap["id"] = "$timestamp"
        hashmap["category"] = category
        hashmap["timestamp"] = timestamp
        hashmap["uid"] = "${firebaseAuth.uid}"

        // DB goc -> san pham -> id -> tt
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Thêm thành công...", Toast.LENGTH_SHORT).show()
                binding.categoryEt.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Thêm không thành công vì ${e.message}", Toast.LENGTH_SHORT).show()
                binding.categoryEt.setText("")
            }
    }
}