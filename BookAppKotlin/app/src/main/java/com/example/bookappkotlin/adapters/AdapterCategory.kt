package com.example.bookappkotlin.adapters

import android.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.databinding.RowCategoryBinding
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.example.bookappkotlin.filters.FilterCategory
import com.example.bookappkotlin.models.ModelCategory
import com.example.bookappkotlin.activities.PdfListAdminActivity
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context

    public var categoryArrayList: ArrayList<ModelCategory>

    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    // constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        // inflate/ bind r_c.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // Lay du lieu, khoi tao, an vao de xoa
        // Lay du lieu
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        // khoi tao du lieu
        holder.categoryTv.text = category

        // an vao o xoa
        holder.deleteBtn.setOnClickListener {
            // Tao khung tuy chon
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Thông báo!")
                .setMessage("Bạn có muốn xóa cuốn sách này không?")
                .setPositiveButton("Có") {a, d ->
                    Toast.makeText(context, "Đang xóa sách...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Không") {a, d ->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        // Lay id sach
        val id = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Toast.makeText(context, "Không thể xóa bởi vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    // Giu khung hinh va cau tao nen UI row_category.xml
    inner class HolderCategory (itemView: View): RecyclerView.ViewHolder (itemView) {

        // Khoi tao
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null)
        {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}