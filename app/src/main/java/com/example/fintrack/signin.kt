package com.example.fintrack

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class signin : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)



        val back : ImageView = findViewById(R.id.btnmenu)

        back.setOnClickListener {
            val intent = Intent(this, loginSignupPage::class.java)
            startActivity(intent)
        }



        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSignin = findViewById<Button>(R.id.btnSignin)
        val tvSignup = findViewById<TextView>(R.id.tvsignup)

        btnSignin.setOnClickListener {
            if (validateLogin()) {

                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, home::class.java))
                finish()
            }
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
            finish()
        }
    }

    private fun validateLogin(): Boolean {
        val edtEmail = findViewById<EditText>(R.id.edtemailin)
        val edtPassword = findViewById<EditText>(R.id.edtpasswordin)

        if (edtEmail.text.toString().trim().isEmpty()) {
            edtEmail.error = "Email is required"
            return false
        }

        if (edtPassword.text.toString().isEmpty()) {
            edtPassword.error = "Password is required"
            return false
        }


        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")

        if (savedEmail != edtEmail.text.toString()) {
            edtEmail.error = "Email not registered"
            return false
        }

        if (savedPassword != edtPassword.text.toString()) {
            edtPassword.error = "Incorrect password"
            return false
        }

        return true
    }
}