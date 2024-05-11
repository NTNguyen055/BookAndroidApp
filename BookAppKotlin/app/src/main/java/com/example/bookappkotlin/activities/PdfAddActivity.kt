package com.example.bookappkotlin.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.databinding.ActivityPdfAddBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri: Uri? = null

    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Sau khi click, hiện hộp thoại chọn sản phẩm
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // Validate data
        Log.d(TAG, "validateData: Kiểm tra dữ liệu")

        // Lay du lieu
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập tiêu đề...", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập đường dẫn...", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty())
        {
            Toast.makeText(this, "Hãy chọn sản phẩm...", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null)
        {
            Toast.makeText(this, "Chọn ảnh (PDF)...", Toast.LENGTH_SHORT).show()
        }
        else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: Đang tải lên kho")

        progressDialog.setMessage("Đang tải PDF...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile (pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: Đã tải lên và đang lấy url...")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb (uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfToStorage: Không tải lên được vì ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Không tải lên được vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: Đang tải lên db")
        progressDialog.setMessage("Đang tải thông tin ảnh...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: Đã tải lên db")
                progressDialog.dismiss()
                Toast.makeText(this, "Đã tải lên...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfInfoToDb: Không tải lên được vì ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Không tải lên được vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Cập nhật pdf vào category")

        // Khoi tao danh sach
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children)
                {
                    var model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Hiển thị pdf của category trong PickDialog")

        // Lấy chuỗi mảng của sản phẩm từ danh sách mảng sp
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices)
        {
            categoriesArray[i] = categoryArrayList[i].category
        }
        // Bang thong bao
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lấy sản phẩm")
            .setItems(categoriesArray) {dialog, which ->
                // Da an vao item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                // Hien sp len textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked ")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF Pick cancelled ")
                Toast.makeText(this, "Đã hủy!", Toast.LENGTH_SHORT).show()
            }
        }
    )
}