package com.example.fintrack

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

class analysis : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var expensePieChart: PieChart
    private lateinit var incomePieChart: PieChart
    private lateinit var summaryText: TextView
    private lateinit var categorySummaryText: TextView





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        pieChart = findViewById(R.id.pieChart)
        expensePieChart = findViewById(R.id.expensePieChart)
        incomePieChart = findViewById(R.id.incomePieChart)
        summaryText = findViewById(R.id.summaryText)
        categorySummaryText = findViewById(R.id.categorySummaryText)

        setupCharts()
        loadFinancialData()


        val back : ImageView = findViewById(R.id.btnmenu)

        back.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }


    }

    private fun setupCharts() {

        setupPieChart(pieChart)


        setupPieChart(expensePieChart).apply {
            centerText = "Expense\nCategories"
        }


        setupPieChart(incomePieChart).apply {
            centerText = "Income\nCategories"
        }
    }

    private fun setupPieChart(chart: PieChart): PieChart {
        return chart.apply {

            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(20f, 10f, 20f, 10f)
            dragDecelerationFrictionCoef = 0.95f
            setDrawEntryLabels(false)


            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)


            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)


            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 5f
                yOffset = 10f
                textColor = Color.WHITE
                textSize = 12f
            }
        }
    }

    private fun loadFinancialData() {

        val balancePref = getSharedPreferences("Balance", Context.MODE_PRIVATE)
        val availableBalance = balancePref.getFloat("available_balance", 0f)

        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        val transactionCount = sharedPref.getInt("transaction_count", 0)

        var totalIncome = 0f
        var totalExpense = 0f
        val incomeCategories = mutableMapOf<String, Float>()
        val expenseCategories = mutableMapOf<String, Float>()

        for (i in 1..transactionCount) {
            val type = sharedPref.getString("type_$i", "")
            val amountStr = sharedPref.getString("amount_$i", "0")
            val category = sharedPref.getString("category_$i", "Uncategorized") ?: "Uncategorized"
            val amount = amountStr?.replace("[^\\d.]".toRegex(), "")?.toFloatOrNull() ?: 0f

            when (type) {
                "Income" -> {
                    totalIncome += amount
                    incomeCategories[category] = (incomeCategories[category] ?: 0f) + amount
                }
                "Expense" -> {
                    totalExpense += amount
                    expenseCategories[category] = (expenseCategories[category] ?: 0f) + amount
                }
            }
        }


        val mainEntries = mutableListOf<PieEntry>().apply {
            if (totalIncome > 0) add(PieEntry(totalIncome, "Income"))
            if (totalExpense > 0) add(PieEntry(totalExpense, "Expense"))
        }


        val expenseEntries = expenseCategories.map { PieEntry(it.value, it.key) }
        val incomeEntries = incomeCategories.map { PieEntry(it.value, it.key) }

        if (mainEntries.isEmpty()) {
            showEmptyState()
            return
        }

        updateMainPieChart(mainEntries)
        updateCategoryPieCharts(expenseEntries, incomeEntries)
        showFinancialSummary(totalIncome, totalExpense, availableBalance)
        showCategorySummary(expenseCategories, incomeCategories)
    }

    private fun updateMainPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = listOf(
                Color.parseColor("#4CAF50"),  // Green for Income
                Color.parseColor("#F44336")  // Red for Expense
            )
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.3f
            valueLinePart2Length = 0.4f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(pieChart)
        }

        pieChart.apply {
            data = PieData(dataSet).apply {
                setValueTextSize(12f)
                setValueTextColor(Color.WHITE)
            }
            centerText = generateCenterText(entries.sumByDouble { it.value.toDouble() }.toFloat())

            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun updateCategoryPieCharts(expenseEntries: List<PieEntry>, incomeEntries: List<PieEntry>) {

        if (expenseEntries.isNotEmpty()) {
            val expenseDataSet = PieDataSet(expenseEntries, "").apply {
                colors = generateColors(expenseEntries.size, 0.7f) // Red tones
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return NumberFormat.getCurrencyInstance().format(value.toDouble())
                    }
                }
            }

            expensePieChart.apply {
                data = PieData(expenseDataSet)
                animateY(1000, Easing.EaseInOutQuad)
                invalidate()
                visibility = View.VISIBLE
            }
        } else {
            expensePieChart.visibility = View.GONE
        }


        if (incomeEntries.isNotEmpty()) {
            val incomeDataSet = PieDataSet(incomeEntries, "").apply {
                colors = generateColors(incomeEntries.size, 0.3f) // Green tones
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return NumberFormat.getCurrencyInstance().format(value.toDouble())
                    }
                }
            }

            incomePieChart.apply {
                data = PieData(incomeDataSet)
                animateY(1000, Easing.EaseInOutQuad)
                invalidate()
                visibility = View.VISIBLE
            }
        } else {
            incomePieChart.visibility = View.GONE
        }
    }

    private fun generateColors(count: Int, hue: Float): List<Int> {
        val colors = mutableListOf<Int>()
        val saturation = 0.8f
        val value = 0.9f

        for (i in 0 until count) {
            val hueOffset = (i.toFloat() / count) * 0.2f // Small variation in hue
            colors.add(Color.HSVToColor(floatArrayOf((hue + hueOffset) * 360, saturation, value)))
        }

        return colors
    }

    private fun generateCenterText(totalAmount: Float): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        return "Total\n${format.format(totalAmount)}"
    }

    private fun showFinancialSummary(totalIncome: Float, totalExpense: Float, balance: Float) {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2

        val netSavings = totalIncome - totalExpense
        val savingsPercentage = if (totalIncome > 0) (netSavings / totalIncome * 100) else 0f

        val summary = """
            Financial Summary
            
            Total Income: ${format.format(totalIncome)}
            Total Expenses: ${format.format(totalExpense)}
            Net Savings: ${format.format(netSavings)} (${"%.1f".format(savingsPercentage)}%)
            Available Balance: ${format.format(balance)}
        """.trimIndent()

        summaryText.text = summary
    }

    private fun showCategorySummary(expenseCategories: Map<String, Float>, incomeCategories: Map<String, Float>) {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2

        val summary = StringBuilder()

        if (expenseCategories.isNotEmpty()) {
            summary.append("Expense Breakdown:\n")
            expenseCategories.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                summary.append("• $category: ${format.format(amount)}\n")
            }
            summary.append("\n")
        }

        if (incomeCategories.isNotEmpty()) {
            summary.append("Income Breakdown:\n")
            incomeCategories.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                summary.append("• $category: ${format.format(amount)}\n")
            }
        }

        categorySummaryText.text = summary.toString().trim()
    }

    private fun showEmptyState() {
        pieChart.apply {
            centerText = "No financial data\navailable"
            data = null
            invalidate()
        }
        summaryText.text = "Add transactions to see your financial overview"
        categorySummaryText.text = ""
        expensePieChart.visibility = View.GONE
        incomePieChart.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadFinancialData()
    }



}