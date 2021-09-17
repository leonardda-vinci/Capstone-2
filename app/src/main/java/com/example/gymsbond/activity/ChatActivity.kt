package com.example.gymsbond.activity

import android.app.Notification
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.gymsbond.R
import com.example.gymsbond.RetrofitInstance
import com.example.gymsbond.adapter.ChatAdapter
import com.example.gymsbond.adapter.UserAdapter
import com.example.gymsbond.databinding.ActivityChatBinding
import com.example.gymsbond.model.Chat
import com.example.gymsbond.model.NotificationData
import com.example.gymsbond.model.PushNotification
import com.example.gymsbond.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    var firebaseUser: FirebaseUser? = null
    var reference:DatabaseReference? = null
    var chatList = ArrayList<Chat>()
    var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)


        var intent = getIntent()
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)
                binding.userNameTv.text = user!!.userName
                if(user.imgProfile == ""){
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                }else{
                    Glide.with(this@ChatActivity).load(user.imgProfile).into(binding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.sendBtn.setOnClickListener {
            var message:String = binding.messageEt.text.toString()

            if (message.isEmpty()){
                Toast.makeText(applicationContext, "Message is empty", Toast.LENGTH_LONG).show()
                binding.messageEt.setText("")
            }else{
                sendMessage(firebaseUser!!.uid,userId, message)
                binding.messageEt.setText("")
                topic = "/topics/$userId"
                PushNotification(NotificationData(userName!!,message),
                topic).also {
                    sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser!!.uid,userId)
    }

    //create send message function
    private fun sendMessage(senderId:String,receiverId:String,message:String){

        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference!!.child("Chat").push().setValue(hashMap)


    }

    fun readMessage(senderId: String, receiverId: String){
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener((object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if(chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)){
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)
                binding.chatRecyclerView.adapter = chatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }))
    }

    private fun sendNotification(notification:PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try{
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Toast.makeText(this@ChatActivity,"Response ${Gson().toJson(response)}", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@ChatActivity,response.errorBody().toString(), Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

}