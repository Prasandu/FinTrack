package com.example.fintrack

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class account : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)


        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)

        val back: ImageView = findViewById(R.id.btnmenu)
        val btnLogout: Button = findViewById(R.id.btnlogout)

        loadUserData()

        back.setOnClickListener {
            startActivity(Intent(this, home::class.java))
            finish()
        }

        btnLogout.setOnClickListener {

            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, signin::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserData() {
        val edtName = findViewById<EditText>(R.id.edtname)
        val edtEmail = findViewById<EditText>(R.id.edtemailin)
        val edtMobile = findViewById<EditText>(R.id.edtMobile)
        val edtPassword = findViewById<EditText>(R.id.edtpasswordin)

        edtName.setText(sharedPreferences.getString("name", ""))
        edtEmail.setText(sharedPreferences.getString("email", ""))
        edtMobile.setText(sharedPreferences.getString("mobile", ""))
        edtPassword.setText(sharedPreferences.getString("password", ""))


        edtName.isEnabled = false
        edtEmail.isEnabled = false
        edtMobile.isEnabled = false
        edtPassword.isEnabled = false
    }
}