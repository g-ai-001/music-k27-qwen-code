package app.music_k27_qwen_code.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "MusicApp"
    private const val MAX_LOG_FILE_SIZE = 2 * 1024 * 1024L // 2MB
    private const val MAX_LOG_FILES = 5

    private var logDir: File? = null
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        logDir = File(dir, "logs")
        logDir?.let {
            if (!it.exists()) it.mkdirs()
            rotateLogsIfNeeded(it)
            logFile = File(it, "app_${fileDateFormat.format(Date())}.log")
        }
    }

    private fun rotateLogsIfNeeded(dir: File) {
        val files = dir.listFiles { f -> f.name.endsWith(".log") }?.sortedBy { it.lastModified() } ?: return
        if (files.size >= MAX_LOG_FILES) {
            files.take(files.size - MAX_LOG_FILES + 1).forEach { it.delete() }
        }
    }

    private fun checkLogFileSize() {
        val file = logFile ?: return
        if (file.length() > MAX_LOG_FILE_SIZE) {
            logDir?.let { dir ->
                val index = dir.listFiles { f -> f.name.endsWith(".log") }?.size ?: 0
                logFile = File(dir, "app_${fileDateFormat.format(Date())}_$index.log")
            }
        }
    }

    private val logLock = Any()

    private fun writeToFile(level: String, msg: String) {
        try {
            synchronized(logLock) {
                checkLogFileSize()
                logFile?.appendText("${dateFormat.format(Date())} [$level] $msg\n")
            }
        } catch (_: Exception) {}
    }

    fun d(msg: String) {
        Log.d(TAG, msg)
        writeToFile("DEBUG", msg)
    }

    fun i(msg: String) {
        Log.i(TAG, msg)
        writeToFile("INFO", msg)
    }

    fun w(msg: String) {
        Log.w(TAG, msg)
        writeToFile("WARN", msg)
    }

    fun e(msg: String, tr: Throwable? = null) {
        Log.e(TAG, msg, tr)
        writeToFile("ERROR", "$msg ${tr?.stackTraceToString() ?: ""}")
    }
}
