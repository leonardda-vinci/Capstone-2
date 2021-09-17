package com.example.gymsbond.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.gymsbond.R
import com.example.gymsbond.adapter.UserAdapter
import com.example.gymsbond.databinding.ActivityUsersBinding
import com.example.gymsbond.firebase.FirebaseService
import com.example.gymsbond.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding

    var userList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseService.sharedPref = getSharedPreferences("sharePref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        binding.userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfile.setOnClickListener {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        getUsersList()

    }


    //create method to get users
    fun getUsersList(){
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")


        val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.getValue(User::class.java)

                if(currentUser!!.imgProfile == ""){
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                }else{
                    Glide.with(this@UsersActivity).load(currentUser.imgProfile).into(binding.imgProfile)
                }

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (!user!!.userId.equals(firebase.uid)) {

                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@UsersActivity,userList)

                binding.userRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }
}
