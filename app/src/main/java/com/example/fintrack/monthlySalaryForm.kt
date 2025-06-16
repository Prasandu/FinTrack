package com.example.fintrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class monthlySalaryForm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_salary_form)



        val back : ImageView = findViewById(R.id.btnmenu)

        back.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }




        val button: Button = findViewById(R.id.btnSubmitMonthlySalary)
        val salaryEditText: EditText = findViewById(R.id.addTitle)

        button.setOnClickListener {
            val salary = salaryEditText.text.toString().trim()

            if (salary.isEmpty()) {
                Toast.makeText(this, "Please enter your monthly salary", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val salaryValue = salary.toFloatOrNull()
            if (salaryValue == null) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPref = getSharedPreferences("Salary", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putFloat("monthly_salary", salaryValue)
                apply()
            }


            val balancePref = getSharedPreferences("Balance", Context.MODE_PRIVATE)
            if (!balancePref.contains("available_balance")) {
                with(balancePref.edit()) {
                    putFloat("available_balance", salaryValue)
                    apply()
                }
            }

            startActivity(Intent(this, home::class.java))
            finish()
        }
    }
}