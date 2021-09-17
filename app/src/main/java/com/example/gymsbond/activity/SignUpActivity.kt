package com.example.gymsbond.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.gymsbond.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signUpBtn.setOnClickListener {
            val userName = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val confirmPassword = binding.confirmPasswordEt.text.toString()

            if(TextUtils.isEmpty(userName)){
                Toast.makeText(applicationContext,"Username is required", Toast.LENGTH_SHORT).show()
            }

            if(TextUtils.isEmpty(email)){
                Toast.makeText(applicationContext,"Email is required", Toast.LENGTH_SHORT).show()
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext,"Password is required", Toast.LENGTH_SHORT).show()
            }

            if(TextUtils.isEmpty(confirmPassword)){
                Toast.makeText(applicationContext,"Confirm password is required", Toast.LENGTH_SHORT).show()
            }

            if(password != confirmPassword){
                Toast.makeText(applicationContext,"Password not match", Toast.LENGTH_SHORT).show()
            }
            registerUser(userName,email,password)
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun registerUser(userName:String, email:String, password: String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    val userId:String = user!!.uid

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashMap:HashMap<String,String> = HashMap()
                    hashMap.put("userId",userId)
                    hashMap.put("userName",userName)
                    hashMap.put("profileImage","")

                    databaseReference.setValue(hashMap).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            //open chat activity
                            binding.nameEt.setText("")
                            binding.emailEt.setText("")
                            binding.passwordEt.setText("")
                            binding.confirmPasswordEt.setText("")
                            val intent = Intent(this@SignUpActivity,
                                UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
    }
}