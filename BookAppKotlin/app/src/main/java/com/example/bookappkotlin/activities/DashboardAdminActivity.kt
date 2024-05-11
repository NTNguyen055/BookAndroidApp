package com.example.bookappkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.BooksUserFragment
import com.example.bookappkotlin.adapters.AdapterCategory
import com.example.bookappkotlin.databinding.ActivityDashboardAdminBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity( Intent(this, CategoryAddActivity::class.java))
        }

        binding.addPdfFab.setOnClickListener {
            startActivity( Intent(this, PdfAddActivity::class.java))
        }


        //click vao nut mo profile
        binding.profileBtn.setOnClickListener {
            startActivity( Intent(this, ProfileActivity::class.java))
        }

    }

    private fun loadCategories() {
        // Khoi tao arraylist
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Don trong mang truoc khi them du lieu
                categoryArrayList.clear()
                for (ds in snapshot.children)
                {
                    // Lay du lieu mau
                    val model = ds.getValue(ModelCategory::class.java)

                    // Them vao arraylist
                    categoryArrayList.add(model!!)
                }
                // Tao bo chuyen doi
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)

                // Tao bo chuyen doi den recycleview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        // Lấy giá trị hiện tại
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null)
        {
            // Không đăng nhập
            startActivity( Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            // Đã đăng nhập
            val email = firebaseUser.email

            binding.subTitleTv.text = email

        }
    }
}
