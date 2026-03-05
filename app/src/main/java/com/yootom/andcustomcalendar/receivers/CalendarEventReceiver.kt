package com.yootom.andcustomcalendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * [CWE-926] Exported BroadcastReceiver - 권한 보호 없이 외부에 노출
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 *
 * 악의적인 앱이 이 리시버에 임의의 Intent를 보낼 수 있음
 */
class CalendarEventReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CalendarReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // [CWE-925] Improper Verification of Intent by Broadcast Receiver
        // Intent 발신자를 검증하지 않고 데이터를 처리
        val action = intent.getStringExtra("action")
        val data = intent.getStringExtra("data")
        val command = intent.getStringExtra("command")

        Log.d(TAG, "Received broadcast - action: $action, data: $data")

        when (action) {
            "delete" -> {
                // 인증 없이 데이터 삭제 실행
                context.deleteDatabase("appDatabase.db")
                Log.d(TAG, "Database deleted via broadcast")
            }
            "export" -> {
                // 인증 없이 데이터 내보내기
                val file = java.io.File(context.filesDir, "exported_data.json")
                file.writeText(data ?: "")
                Log.d(TAG, "Data exported to ${file.absolutePath}")
            }
            "exec" -> {
                // [CWE-078] Command Injection via Broadcast
                if (command != null) {
                    try {
                        val process = Runtime.getRuntime().exec(command)
                        val output = process.inputStream.bufferedReader().readText()
                        Log.d(TAG, "Command output: $output")
                    } catch (e: Exception) {
                        Log.e(TAG, "Command execution failed: ${e.stackTraceToString()}")
                    }
                }
            }
        }
    }
}

