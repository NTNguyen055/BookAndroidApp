package com.example.bookappkotlin.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.example.bookappkotlin.databinding.RowPdfFavoriteBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    //Context
    private val context: Context

    //Mang chua sach
    private var booksArrayList: ArrayList<ModelPdf>

    private lateinit var binding: RowPdfFavoriteBinding

    //Constructor
    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return booksArrayList.size //Tra ve danh sach of list
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        //Lay du lieu, dat lai du lieu (Users > uid > Favorites
        val model = booksArrayList[position]

        loadBookDetails(model, holder)

        //An vao thi mo thong tin sach
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
        //An vao xoa khoi muc yeu thich
        binding.removeFavBtn.setOnClickListener {
            MyApplication.removeFavorite(context, model.id)
        }
    }

    //Tao 1 cai class nho de quan li UI view cua row_pdf_favorite.xml
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init UI views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }

    private fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //Lay du lieu cua sach theo bookid
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //Dat lai du lieu cho mau
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()

                    //Format thoi gian
                    val date = MyApplication.formatTimestamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId", holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

}