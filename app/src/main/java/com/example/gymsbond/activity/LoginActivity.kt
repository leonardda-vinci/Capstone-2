package com.example.gymsbond.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.gymsbond.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser!!

        //check if user login then navigate to user screen
        if(firebaseUser != null){
            val intent = Intent(this@LoginActivity,
                UsersActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext, "Email and password are required",Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this){
                        if (it.isSuccessful){
                            binding.emailEt.setText("")
                            binding.passwordEt.setText("")
                            val intent = Intent(this@LoginActivity,
                                UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Toast.makeText(applicationContext,"Email or password are invalid",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.signUpBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

