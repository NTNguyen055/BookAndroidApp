package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterPdfUser
import com.example.bookappkotlin.models.ModelPdf

class FilterPdfUser : Filter {
//    Mang luu danh sach muon tim kiem
    var filterList: ArrayList<ModelPdf>

//    Con tro
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
//        Gia tri tim kiem
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty())
        {
//            Chu hoa vs chu thuong nhu nhau
            constraint = constraint.toString().uppercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices)
            {
//                Neu co trong db thi hien len va add vao list
                if (filterList[i].title.uppercase().contains(constraint))
                {
                    filteredModels.add(filterList[i])
                }
            }
//            Tra ve so luong va danh sach
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
//        Cho phep thay doi bo loc
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        adapterPdfUser.notifyDataSetChanged()
    }
}