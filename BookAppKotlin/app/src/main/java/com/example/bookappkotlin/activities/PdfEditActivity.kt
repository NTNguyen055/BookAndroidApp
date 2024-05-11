package com.example.bookappkotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfEditBinding

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var bookId = ""

    private lateinit var progressDialog: ProgressDialog

//    ArrayList chua tieu de sach
    private lateinit var categoryTitleArrayList: ArrayList<String>

//    ArrayList chua id sach
    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Lay id sach de sua thong tin
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        loadCategories()
        loadBookInfo()
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Đang load thông tin sách")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay thong tin
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    //Dat lai khung hinh
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //Load lai thong tin sach dung bookId
                    Log.d(TAG, "onDataChange: Đang load lai thông tin sách")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Category")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Lay san pham
                                val category = snapshot.child("category").value

                                //Dat lai
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        // Lay du lieu
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        // Kiem tra du lieu
        if (title.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập tiêu đề", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập đường dẫn", Toast.LENGTH_SHORT).show()
        }
        else if (selectedCategoryId.isEmpty())
        {
            Toast.makeText(this, "Hãy chọn sản phẩm", Toast.LENGTH_SHORT).show()
        }
        else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Bắt đầu cập nhật...")

        progressDialog.setMessage("Đang cập nhật lại thông tin")
        progressDialog.show()

//        Dat lai du lieu cho firebase
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

//        Cap nhat thong tin sach
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Cập nhật thành công...")
                Toast.makeText(this, "Cập nhật thành công...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Cập nhật thất bại vì ${e.message}")
                Toast.makeText(this, "Cập nhật thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        //Show bang chon anh or sach

        //Tao chuoi mang tu danh sach mang cua chuoi dau
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices)
        {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //Bang chon
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn sản phẩm")
            .setItems(categoriesArray) {dialog, position ->
//                Nhap chuot chon, luu lai lua chon
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

//                Dat lai textview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: đang load categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()

                for (ds in snapshot.children)
                {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: category ID $id")
                    Log.d(TAG, "onDataChange: category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}