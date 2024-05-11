package com.example.bookappkotlin.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappkotlin.adapters.AdapterPdfAdmin
import com.example.bookappkotlin.databinding.ActivityPdfListAdminBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        // Dung anh cua san pham
        binding.subTitleTv.text = category

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Load sach
        loadPdfList()

        // tim kiem sach
        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                // Loc du lieu
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                }
                catch (e: Exception) {
                    Log.d(TAG, "onTextChange: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun loadPdfList() {
        //init danh sach mang
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Xoa danh sach cu trc khi them du lieu
                    pdfArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        // Lay du lieu
                        val model = ds.getValue(ModelPdf::class.java)
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    //Xay dung con tro
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}