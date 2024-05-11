package com.example.bookappkotlin.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //Set bien chon 1 trong 2: camera va thu vien
    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileIv.setOnClickListener {
            showImageAttachMenu()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Hãy đợi...")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private var name = ""
    private var email = ""
    private fun validateData() {
        //Lay du lieu tu nguoi nhap
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()

        //Chinh sua dieu khien chi tiet
        if (name.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập tên...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show()
        }
        else if (email.isEmpty())
        {
            Toast.makeText(this, "Hãy nhập email...", Toast.LENGTH_SHORT).show()
        }
        else {
            if (imageUri == null)
            {
                //Cap nhat khong kem vs anh
                updateProfile("")
            }
            else {
                //Cap nhat kem vs anh
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Đang cập nhật ảnh!")
        progressDialog.show()

        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        //Luu ben storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
//                Anh da up xong, lay url cua anh
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Tai anh lên that bai vi ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile (uploadedImageUrl: String) {
        progressDialog.setMessage("Đang sửa thông tin...")
        progressDialog.show()

        //Dat lai info cho db
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        hashMap["email"] = "$email"
        if (imageUri != null)
        {
            hashMap["profileImage"] = uploadedImageUrl
        }

        //Sua trong db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Đã sửa thông tin!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Sửa thông tin thất bại vì ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        //Tra ve thong tin cua nguoi dung len man hinh
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Lay thong tin
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //Xay dung du lieu
                    binding.nameEt.setText(name)
                    binding.emailEt.setText(email)

                    //Set hinh anh
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        print("Error: $e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun showImageAttachMenu() {
        //show cac tuy chon menu nho gom camera, thu vien,...
        //Xay dung menu cua so bat len
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Máy ảnh")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Thư viện")
        popupMenu.show()

        //Tuy chon khi click vao
        popupMenu.setOnMenuItemClickListener {item ->
            val id = item.itemId
            if (id == 0)
            {
                pickImageCamera()
            }
            else if (id == 1) {
                pickImageGallary()
            }
            true
        }
    }

    private fun pickImageCamera() {
        //intent phep chon anh tu camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLaucher.launch(intent)
    }

    private fun pickImageGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        gallaryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLaucher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //Lay uri cua anh
            if (result.resultCode == Activity.RESULT_OK)
            {
                val data = result.data
//                imageUri = data!!.data

                //Dat len anh dai dien
                binding.profileIv.setImageURI(imageUri)
            }
            else {
                //Khong thanh cong or huy
                Toast.makeText(this, "Đã hủy!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val gallaryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback <ActivityResult> {result ->
            //Lay uri cua anh
            if (result.resultCode == Activity.RESULT_OK)
            {
                val data = result.data
                imageUri = data!!.data

                //Dat len anh dai dien
                binding.profileIv.setImageURI(imageUri)
            }
            else {
                //Khong thanh cong or huy
                Toast.makeText(this, "Đã hủy!", Toast.LENGTH_SHORT).show()
            }
        }
    )

}