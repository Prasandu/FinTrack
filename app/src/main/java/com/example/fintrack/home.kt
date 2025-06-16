package com.example.fintrack

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        setContentView(R.layout.activity_home)


        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu: ImageView = findViewById(R.id.btnmenu)
        val edit: ImageView = findViewById(R.id.btnedit)
        val transaction: ImageView = findViewById(R.id.btnPlus)


        navigationView.setNavigationItemSelectedListener(this)


        val header = navigationView.getHeaderView(0)
        val tvHeaderEmail = header.findViewById<TextView>(R.id.tvHeaderEmail)
        tvHeaderEmail.text = "fintrack@gmail.com"


        menu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        edit.setOnClickListener {
            startActivity(Intent(this, monthlySalaryForm::class.java))
        }

        transaction.setOnClickListener {
            startActivity(Intent(this, transactionForm::class.java))
        }


        updateUI()
    }

    override fun onResume() {
        super.onResume()

        updateUI()
    }

    private fun updateUI() {

        val format = NumberFormat.getCurrencyInstance().apply {
            maximumFractionDigits = 2

            val symbols = (this as DecimalFormat).decimalFormatSymbols.apply {
                currencySymbol = "Rs. "
            }
            (this as DecimalFormat).decimalFormatSymbols = symbols
        }


        val salaryPref = getSharedPreferences("Salary", Context.MODE_PRIVATE)
        val monthlySalary = salaryPref.getFloat("monthly_salary", 0f)
        findViewById<TextView>(R.id.monthlySalary).text = format.format(monthlySalary)


        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        val transactionCount = sharedPref.getInt("transaction_count", 0)

        var totalIncome = 0f
        var totalExpense = 0f

        for (i in 1..transactionCount) {
            val type = sharedPref.getString("type_$i", "")
            val amountStr = sharedPref.getString("amount_$i", "0")
            val amount = amountStr?.replace("[^\\d.]".toRegex(), "")?.toFloatOrNull() ?: 0f

            when (type) {
                "Income" -> totalIncome += amount
                "Expense" -> totalExpense += amount
            }
        }


        if (monthlySalary > 0 && totalExpense > monthlySalary) {
            showExpenseAlert(monthlySalary, totalExpense)
        }

        val totalsPref = getSharedPreferences("Totals", Context.MODE_PRIVATE)
        with(totalsPref.edit()) {
            putFloat("total_income", totalIncome)
            putFloat("total_expense", totalExpense)
            apply()
        }


        val balancePref = getSharedPreferences("Balance", Context.MODE_PRIVATE)
        val currentBalance = balancePref.getFloat("available_balance", 0f)
        findViewById<TextView>(R.id.tvAmount1).text = format.format(currentBalance)


        findViewById<TextView>(R.id.income).text = format.format(totalIncome)
        findViewById<TextView>(R.id.expense).text = format.format(totalExpense)
    }

    private fun showExpenseAlert(monthlySalary: Float, totalExpense: Float) {

        val format = NumberFormat.getCurrencyInstance().apply {
            maximumFractionDigits = 2

            val symbols = (this as DecimalFormat).decimalFormatSymbols.apply {
                currencySymbol = "Rs. "
            }
            (this as DecimalFormat).decimalFormatSymbols = symbols
        }

        AlertDialog.Builder(this)
            .setTitle("Expense Alert!")
            .setMessage("Your expenses (${format.format(totalExpense)}) have exceeded your monthly salary (${format.format(monthlySalary)}). Please review your spending.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_transactions -> startActivity(Intent(this, transactionHistory::class.java))
            R.id.nav_analysis -> startActivity(Intent(this, analysis::class.java))
            R.id.nav_account -> startActivity(Intent(this, account::class.java))
            R.id.nav_settings -> startActivity(Intent(this, setting::class.java))
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
}