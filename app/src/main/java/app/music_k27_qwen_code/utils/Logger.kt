package app.music_k27_qwen_code.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "MusicApp"
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun init(context: Context) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val logDir = File(dir, "logs")
        if (!logDir.exists()) logDir.mkdirs()
        logFile = File(logDir, "app_${fileDateFormat.format(Date())}.log")
    }

    private fun writeToFile(level: String, msg: String) {
        try {
            logFile?.appendText("${dateFormat.format(Date())} [$level] $msg\n")
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
