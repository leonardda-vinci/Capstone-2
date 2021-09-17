package com.example.gymsbond.activity

import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.gymsbond.R
import com.example.gymsbond.databinding.ActivityProfileBinding
import com.example.gymsbond.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST: Int = 2020

    //create firebase storage variable
    private lateinit var storage:FirebaseStorage
    //create storage reference variable
    private lateinit var storageRef:StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //get current user data from firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.userNameEt.setText(user!!.userName)

                if (user.imgProfile == "") {
                    binding.userImage.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@ProfileActivity).load(user.imgProfile).into(binding.userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.userImage.setOnClickListener {
            chooseImage()
        }

        binding.updateBtn.setOnClickListener {
            uploadImage()
            binding.progressDialog.visibility = View.VISIBLE
        }

    }

    //create choose image function
    private fun chooseImage() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode != null){
            filePath = data!!.data
            try {
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.userImage.setImageBitmap(bitmap)
                binding.updateBtn.visibility = View.VISIBLE
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    //create upload image functions
    private fun uploadImage(){
        if(filePath != null){

            var ref:StorageReference = storageRef.child("image/"+ UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {

                    val hashMap:HashMap<String,String> = HashMap()
                    hashMap.put("userName",binding.userNameEt.text.toString())
                    hashMap.put("profileImage",filePath.toString())
                    databaseReference.updateChildren(hashMap as Map<String, Any>)
                        binding.progressDialog.visibility = View.GONE
                        Toast.makeText(applicationContext, "Uploaded",Toast.LENGTH_SHORT).show()
                        binding.updateBtn.visibility = View.GONE
                    }
                .addOnFailureListener {
                    binding.progressDialog.visibility = View.GONE
                        Toast.makeText(applicationContext, "Failed"+it.message,Toast.LENGTH_SHORT).show()

                }


        }
    }

}