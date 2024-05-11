package com.example.bookappkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.filters.FilterPdfAdmin
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.example.bookappkotlin.activities.PdfEditActivity
import com.example.bookappkotlin.databinding.RowPdfAdminBinding
import com.example.bookappkotlin.models.ModelPdf

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    private var context: Context

    public var pdfArrayList: ArrayList<ModelPdf>
    private val filterList: ArrayList<ModelPdf>

    private lateinit var binding: RowPdfAdminBinding

    private var filter: FilterPdfAdmin? = null

    constructor (context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //Set du lieu, Get du lieu khi click

        //Get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //Chuyen doi timestap thanh dd/mm/yyyy
        val formattedDate = MyApplication.formatTimestamp(timestamp)

        //Dung du lieu
        holder.titleTv.text = title
        holder.description.text = description
        holder.dataTv.text = formattedDate

        //Tai them cac chi tiet khac pdf url, size
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //Load pdf size kich thuoc
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //Show 2 tùy chọn edit và delete
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        //Click vao san pham
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        //Lay id, url, title cua sach
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //Cac tuy chon de show dialog
        val options = arrayOf("Sửa", "Xóa")

        //Thong bao dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tùy chọn")
            .setItems(options) {dialog, position ->
                //Click vao 1 item
                if (position == 0)
                {
                    //Nut sua
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //tim sua sach bang bookId
                    context.startActivity(intent)
                }
                else if (position == 1)
                {
                    //Nut xoa
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getFilter(): Filter {
        if (filter == null)
        {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    // Xem và giữ class cho row_pdf_admin.xml
    inner class HolderPdfAdmin (itemView: View) : RecyclerView.ViewHolder(itemView) {

        // UI xem cua row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val description = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dataTv = binding.dataTv
        val moreBtn = binding.moreBtn
    }
}
