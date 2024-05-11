package com.example.bookappkotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.R
import com.example.bookappkotlin.adapters.AdapterPdfFavorite
import com.example.bookappkotlin.databinding.ActivityProfileBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var booksArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Set gia tri mac dinh
        binding.accountTypeTv.text = "N/A"
        binding.favoriteBookCountTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        loadUserInfo()
        loadFavoriteBooks()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        binding.accountStatusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified)
            {
                //Da xac minh email
                Toast.makeText(this, "Email đã được xác minh!", Toast.LENGTH_SHORT).show()
            }
            else {
                //Chua duoc xac minh
                emailVerificationDialog()
            }
        }
    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác minh Email")
            .setMessage("Bạn có chắn muốn gửi xác minh cho email: ${firebaseUser.email} không?")
            .setPositiveButton("Có") {d, e ->
                sendEmailVerification()
            }
            .setNegativeButton("Không") {d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        progressDialog.setMessage("Đang gửi yêu cầu xách minh cho email: ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Đã gửi yêu cầu xác minh! Vui lòng kiểm tra email: ${firebaseUser.email} của bạn!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gửi yêu cầu xác minh thất bại!", Toast.LENGTH_SHORT).show()
                println("Error: ${e.message}")
            }
    }

    private fun loadUserInfo() {
        //Tra ve thong tin cua nguoi dung len man hinh
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay thong tin
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //Chuyen timestamp (11123131) sang dinh dang ngay (20/ 03/ 2004)
                    val formattedDate = MyApplication.formatTimestamp(timestamp.toLong())

                    //Xay dung du lieu
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //Set hinh anh
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        print("Error: $e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        //Kiem tra da xac minh email hay chua
        if (firebaseUser.isEmailVerified)
        {
            binding.accountStatusTv.text = "Đã xác minh"
        }
        else {
            binding.accountStatusTv.text = "Chưa xác minh"
        }
    }

    private fun loadFavoriteBooks() {
        //init danh sach mang
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear mang cu truoc khi thiet lap du lieu
                    booksArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        //Lay id duy nhat cua sach
                        val bookId = "${ds.child("bookId").value}"

                        //Dat du lieu cho mau
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //Them mau vao danh sach
                        booksArrayList.add(modelPdf)
                    }
                    //Cap nhat so luong sach trong danh sach yeu thich
                    binding.favoriteBookCountTv.text = "${booksArrayList.size}"

                    //Dat bo chuyen doi
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)

                    //Chuyen thanh recyclerview
                    binding.favoriteRv.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}