package com.example.bookappkotlin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.RowCommentBinding
import com.example.bookappkotlin.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {
    //context
    val context: Context

    //Mang luu comment
    val commentArrayList: ArrayList<ModelComment>

    private lateinit var binding: RowCommentBinding

    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        //binding, inflare
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderComment(binding.root)
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        //Lay du lieu comment, dat lai va click
        //Lay du lieu
        val model = commentArrayList[position]

        val id = model.id
        val uid = model.uid
        val bookId = model.bookId
        val timestamp = model.timestamp
        val comment = model.comment

        //Phot mat cho tg
        val date = MyApplication.formatTimestamp(timestamp.toLong())

        //Dat lai du lieu hien thi
        holder.commentTv.text = comment
        holder.dateTv.text = date

        loadUserDetails(model, holder)

        //show thong bao tuy chon xoa comment
        holder.itemView.setOnClickListener {
            //Phai co tai khoan moi xoa va xac dinh user = uid
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid)
            {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        //Alert Dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Xóa comment")
            .setMessage("Bạn có muốn xóa comment này?")
            .setPositiveButton("Có") {d, e ->

                val bookId = model.bookId
                val commentId = model.id

                //Xoa luon comment da chon
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã xóa comment này!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {e ->
                        Toast.makeText(context, "Xóa không thành công!", Toast.LENGTH_SHORT).show()
                        print("Error: ${e.message}")
                    }
            }
            .setNegativeButton("Không") {d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay ten va anh tu db
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //Dat lai du lieu cho layout
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    } catch (e: Exception) {
                        //Chua co avt thi dat avt mac dinh (ic_person_gray.xml)
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView) {
        //Cac bien views trong row_comment.xml
        val profileIv = binding.profileIv
        val nameTv = binding.nameTv
        val dateTv = binding.dateTv
        val commentTv = binding.commentTv
    }

}