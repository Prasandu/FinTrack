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

class signup : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)


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

        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val tvSignin = findViewById<TextView>(R.id.tvsetting)

        btnSignup.setOnClickListener {
            if (validateInputs()) {
                saveUserData()
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, signin::class.java))
                finish()
            }
        }

        tvSignin.setOnClickListener {
            startActivity(Intent(this, signin::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val edtName = findViewById<EditText>(R.id.edtname)
        val edtEmail = findViewById<EditText>(R.id.edtemailin)
        val edtMobile = findViewById<EditText>(R.id.edtmobile)
        val edtPassword = findViewById<EditText>(R.id.edtpasswordin)
        val edtRePassword = findViewById<EditText>(R.id.reenterpassword)

        if (edtName.text.toString().trim().isEmpty()) {
            edtName.error = "Name is required"
            return false
        }

        if (edtEmail.text.toString().trim().isEmpty()) {
            edtEmail.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString()).matches()) {
            edtEmail.error = "Enter valid email"
            return false
        }

        if (edtMobile.text.toString().trim().isEmpty()) {
            edtMobile.error = "Mobile is required"
            return false
        } else if (edtMobile.text.toString().length != 10) {
            edtMobile.error = "Enter valid 10-digit mobile number"
            return false
        }

        if (edtPassword.text.toString().isEmpty()) {
            edtPassword.error = "Password is required"
            return false
        } else if (edtPassword.text.toString().length < 6) {
            edtPassword.error = "Password must be at least 6 characters"
            return false
        }

        if (edtRePassword.text.toString() != edtPassword.text.toString()) {
            edtRePassword.error = "Passwords don't match"
            return false
        }

        return true
    }

    private fun saveUserData() {
        val editor = sharedPreferences.edit()
        editor.putString("name", findViewById<EditText>(R.id.edtname).text.toString())
        editor.putString("email", findViewById<EditText>(R.id.edtemailin).text.toString())
        editor.putString("mobile", findViewById<EditText>(R.id.edtmobile).text.toString())
        editor.putString("password", findViewById<EditText>(R.id.edtpasswordin).text.toString())
        editor.putBoolean("isLoggedIn", false) // Not logged in yet
        editor.apply()
    }
}