package com.example.bookappkotlin

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import java.util.Calendar
import java.util.Locale
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        fun formatTimestamp (timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            //Dinh dang dd/mm/yyyy
            return  DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //function lay kich thuoc anh
        fun loadPdfSize (pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    Log.d(TAG, "loadPdfSize: Đã lấy meta dữ liệu!")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "LoadPdfSize: Kích cơ Bytes $bytes")

                    //Chuyển đổi bytes sang KB/MB
                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb >= 1)
                    {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    }
                    else if (kb >= 1)
                    {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    }
                    else {
                        sizeTv.text = "${String.format("%.2f", bytes)} Bytes"
                    }
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "loadPdfSize: Không lấy được meta dữ liệu bởi vì ${e.message}")
                }
        }
        fun loadPdfFromUrlSinglePage (
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
            ) {

            val TAG = "PDF_THUMBNAIL_TAG"

            // Su dung url de lay file cua meta du lieu tu firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes ->
                    Log.d(TAG, "LoadPdfSize: Kích cơ Bytes $bytes")

                    //Dung PDFView
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError {t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad {nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")

                            //Anh da load xong, co the dung so trang, thumbnail
                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv != null)
                            {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "loadPdfSize: Không lấy được meta dữ liệu bởi vì ${e.message}")
                }
        }
        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //Load sach bang cach su dung categoryId trong fbase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Lay san pham
                        val category:String = "${snapshot.child("category").value}"

                        //Dung san pham
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

        fun deleteBook (context: Context, bookId: String, bookUrl: String, bookTitle: String) {
//            1. context, dc su dung khi gui yeu cau cho thong bao
//            2. bookId, su dung xoa sach tu db
//            3. bookUrl, su dung xoa sach tu fb storage
//            4. bookTitle, show thong bao

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Đang xóa...")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Hãy đợi")
            progressDialog.setMessage("Đang xóa $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Đang xóa trong storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Đã xóa trong storage")
                    Log.d(TAG, "deleteBook: Đang xóa trong database...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Đã xóa thành công...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Đã xóa trong db")
                        }
                        .addOnFailureListener {e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Xóa trong db thất bại vì ${e.message}")
                            Toast.makeText(context, "Xóa thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Xóa trong storage thất bại vì ${e.message}")
                    Toast.makeText(context, "Xóa thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //Lay so luot xem hien tai
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Lay so luot xem
                        var viewsCount = "${snapshot.child("viewsCount").value}"
                        if (viewsCount == "" || viewsCount == "null")
                        {
                            viewsCount = "0"
                        }
                        //Tang luot xem
                        val newViewsCount = viewsCount.toLong() + 1

                        //Dat lai du lieu cho db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //Db new
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        public fun removeFavorite(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFavorite: Dang xoa sach khoi fav")

            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFavorite: Da xoa")
                    Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "removeFavorite: Xoa that bai vi ${e.message}")
                    Toast.makeText(context, "Xóa khỏi danh sach yeu thich that bai vi ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}

