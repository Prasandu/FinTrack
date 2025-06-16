package com.example.fintrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class transactionHistory : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var transactionContainer: LinearLayout
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var currencyFormat: NumberFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)


        currencyFormat = NumberFormat.getCurrencyInstance().apply {
            maximumFractionDigits = 2
            val symbols = (this as DecimalFormat).decimalFormatSymbols.apply {
                currencySymbol = "Rs. "
            }
            (this as DecimalFormat).decimalFormatSymbols = symbols
        }


        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_transactions)


        findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        findViewById<ImageView>(R.id.btnPlus).setOnClickListener {
            startActivity(Intent(this, transactionForm::class.java))
        }

        transactionContainer = findViewById(R.id.transaction_container)
        loadTransactions()
    }

    private fun loadTransactions() {
        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        val transactionCount = sharedPref.getInt("transaction_count", 0)


        val transactionsByCategory = mutableMapOf<String, MutableList<Transaction>>()
        val categoryTotals = mutableMapOf<String, Float>()


        for (i in 1..transactionCount) {
            val title = sharedPref.getString("title_$i", null)
            val amount = sharedPref.getString("amount_$i", null)
            val dateStr = sharedPref.getString("date_$i", null)
            val type = sharedPref.getString("type_$i", null)
            val category = sharedPref.getString("category_$i", null) ?: "Uncategorized"

            if (title != null && amount != null && dateStr != null && type != null) {
                try {
                    val amountValue = amount.replace("[^\\d.]".toRegex(), "").toFloatOrNull() ?: 0f
                    val parsedDate = dateFormat.parse(dateStr) ?: Date(0)

                    val transaction = Transaction(
                        id = i,
                        title = title,
                        amount = amount,
                        dateStr = dateStr,
                        type = type,
                        category = category,
                        date = parsedDate,
                        amountValue = amountValue
                    )


                    if (!transactionsByCategory.containsKey(category)) {
                        transactionsByCategory[category] = mutableListOf()
                        categoryTotals[category] = 0f
                    }
                    transactionsByCategory[category]?.add(transaction)


                    if (type == "Expense") {
                        categoryTotals[category] = categoryTotals[category]?.plus(amountValue) ?: amountValue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        saveCategoryTotals(categoryTotals)


        transactionContainer.removeAllViews()


        for ((category, transactions) in transactionsByCategory) {

            transactions.sortByDescending { it.date }


            addCategoryHeader(category, categoryTotals[category] ?: 0f)


            for (transaction in transactions) {
                addTransactionView(transaction)
            }
        }
    }

    private fun addCategoryHeader(category: String, total: Float) {
        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.item_category_header, transactionContainer, false)

        headerView.findViewById<TextView>(R.id.tvCategoryName).text = category
        headerView.findViewById<TextView>(R.id.tvCategoryTotal).text =
            "Total: ${currencyFormat.format(total)}"

        transactionContainer.addView(headerView)
    }

    private fun addTransactionView(transaction: Transaction) {
        val inflater = LayoutInflater.from(this)
        val transactionView = inflater.inflate(R.layout.item_transaction, transactionContainer, false)


        val backgroundRes = when (transaction.type) {
            "Income" -> R.drawable.rectangle_income
            "Expense" -> R.drawable.rectangle_expense
            else -> R.drawable.rectangle_income
        }
        transactionView.findViewById<ImageView>(R.id.transactionBackground).setImageResource(backgroundRes)

        val textColor = when (transaction.type) {
            "Income" -> ContextCompat.getColor(this, R.color.green)
            "Expense" -> ContextCompat.getColor(this, R.color.red)
            else -> ContextCompat.getColor(this, android.R.color.black)
        }

        transactionView.findViewById<TextView>(R.id.tvTitle).text = transaction.title
        transactionView.findViewById<TextView>(R.id.tvAmount).text =
            currencyFormat.format(transaction.amountValue)
        transactionView.findViewById<TextView>(R.id.tvAmount).setTextColor(textColor)
        transactionView.findViewById<TextView>(R.id.tvDate).text = transaction.dateStr
        transactionView.findViewById<TextView>(R.id.tvType).text = transaction.type
        transactionView.findViewById<TextView>(R.id.tvType).setTextColor(textColor)


        transactionView.findViewById<ImageView>(R.id.btnDelete).setOnClickListener { view ->
            showDeleteConfirmation(transaction.id, view)
        }

        transactionContainer.addView(transactionView)
    }

    private fun saveCategoryTotals(categoryTotals: Map<String, Float>) {
        val analysisPref = getSharedPreferences("Analysis", Context.MODE_PRIVATE)
        with(analysisPref.edit()) {
            categoryTotals.forEach { (category, total) ->
                putFloat("category_$category", total)
            }
            apply()
        }
    }

    private fun showDeleteConfirmation(position: Int, view: View) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction(position: Int) {
        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        val transactionCount = sharedPref.getInt("transaction_count", 0)

        if (position <= transactionCount) {

            val amountStr = sharedPref.getString("amount_$position", "0")
            val type = sharedPref.getString("type_$position", "")
            val amount = amountStr?.replace("[^\\d.]".toRegex(), "")?.toFloatOrNull() ?: 0f


            for (i in position until transactionCount) {
                sharedPref.edit().apply {
                    putString("title_$i", sharedPref.getString("title_${i+1}", ""))
                    putString("amount_$i", sharedPref.getString("amount_${i+1}", ""))
                    putString("date_$i", sharedPref.getString("date_${i+1}", ""))
                    putString("type_$i", sharedPref.getString("type_${i+1}", ""))
                    putString("category_$i", sharedPref.getString("category_${i+1}", ""))
                    apply()
                }
            }


            sharedPref.edit().apply {
                remove("title_$transactionCount")
                remove("amount_$transactionCount")
                remove("date_$transactionCount")
                remove("type_$transactionCount")
                remove("category_$transactionCount")
                putInt("transaction_count", transactionCount - 1)
                apply()
            }


            if (type == "Income" || type == "Expense") {
                updateBalanceAfterDelete(type, amount)
            }


            loadTransactions()
        }
    }

    private fun updateBalanceAfterDelete(type: String, amount: Float) {
        val balancePref = getSharedPreferences("Balance", Context.MODE_PRIVATE)
        val currentBalance = balancePref.getFloat("available_balance", 0f)

        balancePref.edit().apply {
            if (type == "Income") {
                putFloat("available_balance", currentBalance - amount)
            } else if (type == "Expense") {
                putFloat("available_balance", currentBalance + amount)
            }
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_transactions)
        loadTransactions()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, home::class.java))
                finish()
            }
            R.id.nav_transactions -> drawerLayout.closeDrawer(GravityCompat.START)
            R.id.nav_analysis -> {
                startActivity(Intent(this, analysis::class.java))
                finish()
            }
            R.id.nav_account -> {
                startActivity(Intent(this, account::class.java))
                finish()
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, setting::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    data class Transaction(
        val id: Int,
        val title: String,
        val amount: String,
        val dateStr: String,
        val type: String,
        val category: String,
        val date: Date,
        val amountValue: Float
    )
}