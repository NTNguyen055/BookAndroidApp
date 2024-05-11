package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterCategory
import com.example.bookappkotlin.models.ModelCategory

class FilterCategory: Filter {
    // Danh sach khi tim kiem
    private var filterList: ArrayList<ModelCategory>

    //
    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        // gia tri khong rong va khong null
        if (constraint != null && constraint.isNotEmpty())
        {
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                // Validate
                if (filterList[i].category.uppercase().contains(constraint))
                {
                    // Tao danh sach loc
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else {
            // gia tri null va rong
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // Chap nhan bo loc
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        // Thay doi thong bao
        adapterCategory.notifyDataSetChanged()
    }

}