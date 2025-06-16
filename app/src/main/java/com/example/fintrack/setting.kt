package com.example.fintrack

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back : ImageView = findViewById(R.id.btnmenu)

        back.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }

        val btnBackup = findViewById<Button>(R.id.btnBackup)
        val btnRestore = findViewById<Button>(R.id.btnRestore)

        btnBackup.setOnClickListener {
            backupData()
        }

        btnRestore.setOnClickListener {
            restoreData()
        }
    }

    private fun backupData() {
        try {

            val backupDir = File(getExternalFilesDir(null), "FinTrackBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val filesToBackup = listOf(
                "Salary.xml",
                "Transactions.xml",
                "Totals.xml",
                "Balance.xml"
            )

            var successCount = 0
            filesToBackup.forEach { fileName ->
                val srcFile = File(filesDir, "../shared_prefs/$fileName")
                val destFile = File(backupDir, fileName)

                if (srcFile.exists()) {
                    FileInputStream(srcFile).channel.use { srcChannel ->
                        FileOutputStream(destFile).channel.use { destChannel ->
                            destChannel.transferFrom(srcChannel, 0, srcChannel.size())
                            successCount++
                        }
                    }
                }
            }

            if (successCount > 0) {
                showAlert("Success", "Backup completed successfully to ${backupDir.path}")
            } else {
                showAlert("Warning", "No data found to backup")
            }
        } catch (e: Exception) {
            showAlert("Error", "Backup failed: ${e.message}")
        }
    }

    private fun restoreData() {
        try {

            val backupDir = File(getExternalFilesDir(null), "FinTrackBackup")

            if (!backupDir.exists() || backupDir.listFiles().isNullOrEmpty()) {
                showAlert("Error", "No backup found in ${backupDir.path}")
                return
            }

            val filesToRestore = listOf(
                "Salary.xml",
                "Transactions.xml",
                "Totals.xml",
                "Balance.xml"
            )

            var successCount = 0
            filesToRestore.forEach { fileName ->
                val srcFile = File(backupDir, fileName)
                val destFile = File(filesDir, "../shared_prefs/$fileName")

                if (srcFile.exists()) {
                    try {
                        FileInputStream(srcFile).channel.use { srcChannel ->
                            FileOutputStream(destFile).channel.use { destChannel ->
                                destChannel.transferFrom(srcChannel, 0, srcChannel.size())
                                successCount++
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Restore", "Error restoring $fileName", e)
                    }
                }
            }

            if (successCount > 0) {
                showAlert("Success", "$successCount files restored successfully. Please restart the app.")
            } else {
                showAlert("Warning", "No valid backup files found")
            }
        } catch (e: Exception) {
            showAlert("Error", "Restore failed: ${e.localizedMessage}")
        }
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}