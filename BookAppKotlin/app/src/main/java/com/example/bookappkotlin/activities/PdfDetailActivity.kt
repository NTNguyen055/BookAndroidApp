package com.example.bookappkotlin.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookappkotlin.Constants
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.R
import com.example.bookappkotlin.adapters.AdapterComment
import com.example.bookappkotlin.databinding.ActivityPdfDetailBinding
import com.example.bookappkotlin.databinding.DialogCommentAddBinding
import com.example.bookappkotlin.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //Lay dc tu intent
    private var bookId = ""

    //Lay tu fb
    private var bookTitle = ""
    private var bookUrl = ""

    private var isInMyFavorite = false

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    //Lay cac comment tu danh sach mang ben class ModelComment
    private lateinit var commentArrayList: ArrayList<ModelComment>

    //Tao con tro de chon comment trong recyclerview
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Lay id cua sach dc chon tu trang trc
        bookId = intent.getStringExtra("bookId")!!

        //Bang thong bao
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null)
        {
            checkFavorite()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //Dem so luot xem sau khi vao xem -> luu lai db
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetail()
        showComments()

        binding.downloadBookBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "onCreate: STORAGE PERMISSION đã được cấp phép")
                downloadBook()
            }
            else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION không được cấp phép, hãy gửi lại")
                requestStoragePermissionLaucher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.favoriteBtn.setOnClickListener {
            //Check da dang nhap hay ch
            if (firebaseAuth.currentUser == null)
            {
                //Neu chua dang nhap
                Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            }
            else {
                //Neu da dang nhap, co the click vao favorite
                if (isInMyFavorite)
                {
                    MyApplication.removeFavorite(this, bookId)
                }
                else {
                    addFavorite()
                }
            }
        }

        binding.addCommentBtn.setOnClickListener {
            //Ktra dang nhap hay ch
            if (firebaseAuth.currentUser == null)
            {
                Toast.makeText(this, "Bạn hãy đăng nhập tài khoản!", Toast.LENGTH_SHORT).show()
            }
            else {
                addCommentDialog()
            }
        }
    }

    private fun showComments() {
        //Khoi tao danh sach mang
        commentArrayList = ArrayList()

        //db path de load comments
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Clear danh sach
                    commentArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        //Lay du lieu tung comment
                        val model = ds.getValue(ModelComment::class.java)

                        //Them vao danh sach mang
                        commentArrayList.add(model!!)
                    }
                    //Khoi dung con tro
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)

                    //Nhan cai nao chon comment do
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        //View dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        //Xay dung khung thong bao
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //Tao va show khung tb
        val alertDialog = builder.create()
        alertDialog.show()

        //click tu choi, huy
        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        //click submit, them binh luan
        commentAddBinding.submitBtn.setOnClickListener {
            //lay du lieu
            comment = commentAddBinding.commentEt.text.toString().trim()

            //Kra du lieu
            if (comment.isEmpty())
            {
                Toast.makeText(this, "Hãy nhập bình luận...", Toast.LENGTH_SHORT).show()
            }
            else {
                alertDialog.dismiss()
                addComment()
            }
        }

    }

    private fun addComment() {
        //show tien trinh
        progressDialog.setMessage("Đang thêm bình luận")
        progressDialog.show()

        //timestamp cho id bl, tg bl
        val timestamp = "${System.currentTimeMillis()}"

        //Dat lai du lieu de luu vao data (comment)
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Duong dan db de them vao
        //Books > bookId > Comments > commentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Đã thêm bình luận...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Thêm bình luận không thành công vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val requestStoragePermissionLaucher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted: Boolean ->
        if (isGranted)
        {
            Log.d(TAG, "onCreate: STORAGE PERMISSION đã được cấp phép")
            downloadBook()
        }
        else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION bị từ chối")
            Toast.makeText(this, "Không được cho phép", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Đang tải sách")
        progressDialog.setMessage("Đang tải sách")
        progressDialog.show()

        //Tai sach tu fb bang url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "downloadBook: Sach dang down")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Down sach that bai vi ${e.message}")
                Toast.makeText(this, "Tải sách thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray) {
        Log.d(TAG, "savetoDownloadsFolder: Dang luu sach")

        val nameWithExtention = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //Tao folder moi neu khong ton tai

            val filePath = downloadsFolder.path + "/" + nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Đã lưu vào thư mục tải xuống", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDowmloadsFolder: Đã lưu vào thư mục tải xuống")
            progressDialog.dismiss()

            incrementDownloadCount()
        }
        catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: Lưu thất bại vì ${e.message}")
            Toast.makeText(this, "Lưu thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        //Tang so luong download
        Log.d(TAG, "incrementDownloadCount: ")

        //Lay so luong tai xuong truoc do
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay so luong tai xuong
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: So luong tai xuong hien tai: $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null")
                    {
                        downloadsCount == "0"
                    }
                    //Doi thanh kieu Long va tang 1
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: So luong tai xuong moi: $newDownloadCount")

                    //Dat lai du lieu cho db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //Cap nhat so luong tai xuong moi
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Đã tăng số lượng tải xuống")
                        }
                        .addOnFailureListener {e ->
                            Log.d(TAG, "onDataChange: Tăng số lượng tải xuống thất bại vì ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookDetail() {
        //Books > bookId > detail
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay du lieu cua sach theo bookid
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    //Load pdf
                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    //Load thumnail, so trang
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    //Load kich thuoc pdf
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //Dat lai du lieu
                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun checkFavorite() {
        Log.d(TAG, "checkFavorite: Dang kiem tra sach da co trong fav chua")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite)
                    {
                        Log.d(TAG, "onDataChange: Da co trong danh sach")
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_used_24, 0, 0)
                        binding.favoriteBtn.text = "Remove Favorite"
                    }
                    else {
                        Log.d(TAG, "onDataChange: Chua co trong danh sach")
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_24, 0, 0)
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addFavorite() {
        Log.d(TAG, "addFavorite: Dang them vao favo")
        val timestamp = System.currentTimeMillis()

        //Setup du lieu nap vao
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //Luu vao db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addFavorite: Da them vao fav")
                Toast.makeText(this, "Thêm vào danh sách yêu thích thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "addFavorite: Them voa fav that bai vi ${e.message}")
                Toast.makeText(this, "Thêm vao danh sach yêu thích thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}