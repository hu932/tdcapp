package com.app.helper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class DataReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DataReceiver"
        const val ACTION_FILE = "com.app.helper.FILE_ACTION"
        const val ACTION_LOGS = "com.app.helper.LOGS_ACTION"
        const val ACTION_VER = "com.app.helper.VER_ACTION"
        const val ACTION_TOAST = "com.app.helper.TOAST_ACTION"
        const val ACTION_FLOAT = "com.app.helper.SHOW_FLOAT_ACTION"

        /** 超过这个长度就不再走 Intent extra，改为落盘传路径。Binder 单次事务上限约 1MB。 */
        const val INLINE_LIMIT = 128 * 1024

        /** 落盘后传给 Service 的临时文件路径。 */
        const val EXTRA_CONTENT_PATH = "file_content_path"

        var onFileReceived: ((uri: String, path: String, taskId: String) -> Unit)? = null
        var onLogReceived: ((message: String) -> Unit)? = null
        var onVersionReceived: ((version: String) -> Unit)? = null
        var onToastReceived: ((message: String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FILE -> {
                val extraData = intent.getStringExtra("extra_data") ?: ""
                val extraPath = intent.getStringExtra("extra_data_path") ?: ""
                val taskId = intent.getStringExtra("task_id") ?: ""
                Log.d(TAG, "FILE_ACTION received: uri=$extraData path=$extraPath taskId=$taskId")

                val fileContent = readFileContent(context, extraData, extraPath)
                if (fileContent != null) {
                    onFileReceived?.invoke(extraData, extraPath, taskId)
                    val serviceIntent = Intent(context, com.app.helper.service.TaskService::class.java).apply {
                        action = "PROCESS_FILE"
                        putExtra("file_uri", extraData)
                        putExtra("file_path", extraPath)
                        putExtra("task_id", taskId)
                        // 抓包内容动辄 1MB 以上，直接 putExtra 会撞 Binder 1MB 上限，
                        // 在 startService 处抛 TransactionTooLargeException 把进程带崩。
                        // 超过阈值就落盘，只把路径传给 Service。
                        if (fileContent.length > INLINE_LIMIT) {
                            val tmp = spillToCache(context, fileContent)
                            if (tmp != null) {
                                putExtra(EXTRA_CONTENT_PATH, tmp.absolutePath)
                                Log.d(TAG, "内容 ${fileContent.length} 字符已落盘: ${tmp.absolutePath}")
                            } else {
                                // 落盘失败也不能硬塞，宁可本轮放弃，不让进程崩
                                Log.e(TAG, "落盘失败，放弃本轮回传（${fileContent.length} 字符）")
                                return
                            }
                        } else {
                            putExtra("file_content", fileContent)
                        }
                    }
                    try {
                        context.startService(serviceIntent)
                    } catch (t: Throwable) {
                        Log.e(TAG, "startService 失败", t)
                    }
                }
            }
            ACTION_LOGS -> {
                val msg = intent.getStringExtra("extra_data") ?: ""
                Log.d(TAG, "LOG: $msg")
                onLogReceived?.invoke(msg)
            }
            ACTION_VER -> {
                val ver = intent.getStringExtra("extra_data") ?: ""
                Log.d(TAG, "VERSION: $ver")
                onVersionReceived?.invoke(ver)
            }
            ACTION_TOAST -> {
                val msg = intent.getStringExtra("extra_data") ?: ""
                Log.d(TAG, "TOAST: $msg")
                onToastReceived?.invoke(msg)
            }
            ACTION_FLOAT -> {
                Log.d(TAG, "SHOW_FLOAT_ACTION received")
            }
        }
    }

    /** 把大内容写进缓存目录，返回文件；失败返回 null。 */
    private fun spillToCache(context: Context, content: String): java.io.File? {
        return try {
            val dir = java.io.File(context.cacheDir, "inbox").apply { mkdirs() }
            // 顺手清掉上一次遗留的，避免占空间
            dir.listFiles()?.forEach { if (System.currentTimeMillis() - it.lastModified() > 3600_000L) it.delete() }
            val f = java.io.File(dir, "task_${System.currentTimeMillis()}.txt")
            f.writeText(content)
            f
        } catch (e: Exception) {
            Log.e(TAG, "spillToCache 失败", e)
            null
        }
    }

    private fun readFileContent(context: Context, uriStr: String, path: String): String? {
        // Try reading via content URI first
        if (uriStr.startsWith("content://")) {
            try {
                val uri = Uri.parse(uriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    return stream.bufferedReader().readText()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read via URI: $uriStr", e)
            }
        }
        // Fallback to file path
        if (path.isNotEmpty()) {
            try {
                val file = java.io.File(path)
                if (file.exists()) return file.readText()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read file: $path", e)
            }
        }
        return null
    }
}
