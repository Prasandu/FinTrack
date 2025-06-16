package com.example.fintrack

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.*

class transactionForm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_form)


        val back : ImageView = findViewById(R.id.btnmenu)

        back.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }



        val button: Button = findViewById(R.id.btnAdd)
        val dateEditText: EditText = findViewById(R.id.addDate)
        val titleEditText: EditText = findViewById(R.id.addTitle)
        val amountEditText: EditText = findViewById(R.id.addAmount)
        val radioGroup: RadioGroup = findViewById(R.id.typeRadioGroup)
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)


        val categories = arrayOf("Select Category", "Food", "Transport", "Bills", "Entertainment","Others")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter


        dateEditText.isFocusable = false
        dateEditText.isClickable = true

        dateEditText.setOnClickListener {
            showDatePicker(dateEditText)
        }

        button.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val amount = amountEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val category = categorySpinner.selectedItem.toString()

            if (title.isEmpty() || amount.isEmpty() || date.isEmpty() || selectedRadioButtonId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount.toFloatOrNull() == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category == "Select Category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val type = selectedRadioButton.text.toString()

            saveTransaction(title, amount, date, type, category)
            startActivity(Intent(this, home::class.java))
            finish()
        }
    }

    private fun showDatePicker(dateEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay.toString().padStart(2, '0')}/${(selectedMonth + 1).toString().padStart(2, '0')}/$selectedYear"
                dateEditText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveTransaction(title: String, amount: String, date: String, type: String, category: String) {
        val amountValue = amount.toFloat()


        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        val transactionCount = sharedPref.getInt("transaction_count", 0) + 1

        with(sharedPref.edit()) {
            putString("title_$transactionCount", title)
            putString("amount_$transactionCount", amount)
            putString("date_$transactionCount", date)
            putString("type_$transactionCount", type)
            putString("category_$transactionCount", category)
            putInt("transaction_count", transactionCount)
            apply()
        }


        val balancePref = getSharedPreferences("Balance", Context.MODE_PRIVATE)
        var currentBalance = balancePref.getFloat("available_balance", 0f)

        currentBalance = if (type == getString(R.string.selectIncome)) {
            currentBalance + amountValue
        } else {
            currentBalance - amountValue
        }

        with(balancePref.edit()) {
            putFloat("available_balance", currentBalance)
            apply()
        }
    }
}