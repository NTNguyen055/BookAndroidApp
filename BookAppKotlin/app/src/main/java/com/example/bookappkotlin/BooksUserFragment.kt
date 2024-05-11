package com.example.bookappkotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookappkotlin.adapters.AdapterPdfUser
import com.example.bookappkotlin.databinding.FragmentBooksUserBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    public companion object {
        private const val TAG = "BOOK_USER_TAG"

//        Nhan cac gia tri du lieu cua sach khi load books
        public fun newInstance (categoryId: String, category: String, uid: String) : BooksUserFragment {
            val fragment = BooksUserFragment()

//            Dat du lieu cho bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)

            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Nhan cac phuong thuc da thong qua newInstance
        val args = arguments
        if (args != null)
        {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "conCreateView: Category: $category")

        when (category) {
            "All" -> {
                loadAllBook()
            }
            "Most Viewed" -> {
                loadMostViewedDownloadedBooks("viewsCount")
            }
            "Most Downloaded" -> {
                loadMostViewedDownloadedBooks("downloadsCount")
            }
            else -> {
                loadCategorizeBooks()
            }
        }

        binding.searchEt.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: Lỗi tìm kiếm vì ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadMostViewedDownloadedBooks(orderBy: String) {
//        init List
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children)
                {
//                    lay du lieu
                    val model = ds.getValue(ModelPdf::class.java)

//                    Them vao danh sach
                    pdfArrayList.add(model!!)
                }
//                Dung lai con tro chuot
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)

//                Dung con tro chuot cho Recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadAllBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
//                    lay du lieu
                        val model = ds.getValue(ModelPdf::class.java)

//                    Thme vao danh sach
                        pdfArrayList.add(model!!)
                    }
//                Dung lai con tro chuot
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)

//                Dung con tro chuot cho Recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadCategorizeBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
//                    lay du lieu
                        val model = ds.getValue(ModelPdf::class.java)

//                    Thme vao danh sach
                        pdfArrayList.add(model!!)
                    }
//                Dung lai con tro chuot
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)

//                Dung con tro chuot cho Recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}