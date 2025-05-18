package com.example.financetracker.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUtil(private val context: Context) {
    companion object {
        private const val BACKUP_FILE_PREFIX = "BudgetBoss_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }

    fun exportData(data: String): String? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"

        return try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data.toByteArray())
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun importData(filePath: String): String? {
        return try {
            val file = File(filePath)
            FileInputStream(file).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                String(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getBackupFiles(): List<File> {
        val files = context.filesDir.listFiles { file ->
            file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
        }
        return files?.toList() ?: emptyList()
    }
}
