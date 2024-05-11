package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterPdfAdmin
import com.example.bookappkotlin.models.ModelPdf

// Su dung va tim kiem du lieu Recyclerview
class FilterPdfAdmin : Filter {
    //Danh sach tim kiem
    var filterList: ArrayList<ModelPdf>

    var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint //Gia tri tim kiem
        val results = FilterResults()

        //Khong rong va null
        if (constraint != null && constraint.isNotEmpty())
        {
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices)
            {
                //Neu khop vs gia tri thi xac nhan
                if (filterList[i].title.lowercase().contains(constraint))
                {
                    //Con gia tri nao khac giong thi them vao list da search
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else {
            //Neu rong or null thi -> hien all data
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //Ap dung bo loc thay doi
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>
        //Thong bao thay doi
        adapterPdfAdmin.notifyDataSetChanged()
    }
}