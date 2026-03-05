package com.yootom.andcustomcalendar.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.io.File
import java.net.ServerSocket
import java.util.regex.Pattern

/**
 * Android 고유 보안 취약점 모음
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 */
class VulnerableAndroidHelper(private val context: Context) {

    companion object {
        private const val TAG = "AndroidHelper"
    }

    /**
     * [CWE-926] Improper Export of Android Application Components
     * [CWE-927] Implicit Intent로 민감 데이터 전송
     */
    fun shareCalendarData(eventData: String, userToken: String) {
        // Implicit Intent로 민감 정보 전송 - 악성 앱이 가로챌 수 있음
        val intent = Intent("com.yootom.SHARE_CALENDAR")
        intent.putExtra("event_data", eventData)
        intent.putExtra("auth_token", userToken)      // 인증 토큰을 implicit intent로 전송!
        intent.putExtra("user_secret", "my_secret_data")
        context.sendBroadcast(intent)  // 모든 앱이 수신 가능!
    }

    /**
     * [CWE-749] Exposed Dangerous Method
     * WebView에 JavaScript Interface를 노출
     */
    fun setupInsecureWebView(webView: WebView, url: String) {
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true                  // 파일 접근 허용 - 취약점!
        webView.settings.allowUniversalAccessFromFileURLs = true // 파일 URL에서 범용 접근 허용 - 취약점!
        webView.settings.allowFileAccessFromFileURLs = true      // 파일 URL에서 파일 접근 허용 - 취약점!

        // JavaScript에서 Android 코드 실행 가능 - 취약점!
        webView.addJavascriptInterface(VulnerableJSInterface(context), "Android")

        // 사용자 입력 URL을 검증 없이 로드 - 취약점!
        webView.loadUrl(url)
    }

    /**
     * [CWE-749] JavaScript Interface에서 위험한 메서드 노출
     */
    class VulnerableJSInterface(private val context: Context) {

        @JavascriptInterface
        fun getDeviceInfo(): String {
            // 디바이스 정보를 JS에 노출
            return "Model: ${Build.MODEL}, Android: ${Build.VERSION.SDK_INT}, " +
                    "Serial: ${Build.SERIAL}"
        }

        @JavascriptInterface
        fun executeCommand(command: String): String {
            // [CWE-078] OS Command Injection
            // JavaScript에서 시스템 명령 실행 가능 - 매우 위험!
            val process = Runtime.getRuntime().exec(command)
            return process.inputStream.bufferedReader().readText()
        }

        @JavascriptInterface
        fun readFile(filePath: String): String {
            // [CWE-022] Path Traversal via JS Interface
            // JavaScript에서 임의 파일 읽기 가능!
            return File(filePath).readText()
        }

        @JavascriptInterface
        fun writeToPrefs(key: String, value: String) {
            val prefs = context.getSharedPreferences("js_data", Context.MODE_PRIVATE)
            prefs.edit().putString(key, value).apply()
        }
    }

    /**
     * [CWE-078] OS Command Injection
     * 사용자 입력을 직접 시스템 명령에 사용
     */
    fun pingServer(hostname: String): String {
        // 사용자 입력을 검증 없이 exec에 전달 - Command Injection!
        // 입력: "google.com; cat /etc/passwd" → 명령 주입 가능
        val process = Runtime.getRuntime().exec("ping -c 1 $hostname")
        return process.inputStream.bufferedReader().readText()
    }

    /**
     * [CWE-078] OS Command Injection - array 형태
     */
    fun checkDiskSpace(path: String): String {
        val commands = arrayOf("/bin/sh", "-c", "df -h $path")
        val process = Runtime.getRuntime().exec(commands)
        return process.inputStream.bufferedReader().readText()
    }

    /**
     * [CWE-200] Information Exposure
     * 상세한 에러 정보를 사용자에게 노출
     */
    fun processCalendarImport(data: String): String {
        return try {
            // 처리 로직...
            val parsed = data.toInt()
            "Success: $parsed"
        } catch (e: Exception) {
            // 전체 스택 트레이스를 사용자에게 반환 - 정보 노출!
            val stackTrace = e.stackTraceToString()
            Log.e(TAG, "Full error: $stackTrace")
            "Error occurred: ${e.message}\nStack: $stackTrace\nClass: ${e.javaClass.name}"
        }
    }

    /**
     * [CWE-312] Cleartext Storage in SQLite
     * 민감 데이터를 암호화 없이 SQLite에 직접 저장
     */
    inner class InsecureDBHelper(context: Context) :
        SQLiteOpenHelper(context, "user_secrets.db", null, 1) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE user_secrets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT," +
                        "password TEXT," +       // 비밀번호 평문 저장!
                        "ssn TEXT," +            // 주민등록번호 평문 저장!
                        "credit_card TEXT" +     // 신용카드 평문 저장!
                        ")"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS user_secrets")
            onCreate(db)
        }

        /**
         * [CWE-089] SQL Injection in SQLiteOpenHelper
         */
        fun findUser(username: String): Boolean {
            val db = readableDatabase
            // String concatenation으로 SQL Injection 취약!
            val cursor = db.rawQuery(
                "SELECT * FROM user_secrets WHERE username = '$username'", null
            )
            val found = cursor.count > 0
            cursor.close()
            return found
        }

        fun saveUserCredentials(username: String, password: String, ssn: String, cc: String) {
            val db = writableDatabase
            // 평문으로 민감 정보 저장
            db.execSQL(
                "INSERT INTO user_secrets (username, password, ssn, credit_card) " +
                        "VALUES ('$username', '$password', '$ssn', '$cc')"
            )
            Log.d(TAG, "Saved credentials for $username with password $password")
        }
    }

    /**
     * [CWE-1204] Generation of Weak Initialization Vector (IV)
     * 정적 IV를 사용한 암호화
     */
    fun encryptWithStaticIV(plaintext: String): ByteArray {
        val key = javax.crypto.spec.SecretKeySpec("0123456789abcdef".toByteArray(), "AES")
        // 고정된 IV 사용 - 취약점!
        val iv = javax.crypto.spec.IvParameterSpec(ByteArray(16))  // 모두 0인 IV
        val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(plaintext.toByteArray())
    }

    /**
     * [CWE-757] Selection of Less-Secure Algorithm During Negotiation
     * TLSv1.0 사용 (취약한 프로토콜)
     */
    fun createWeakTlsConnection(): javax.net.ssl.SSLContext {
        // TLSv1.0은 취약한 프로토콜
        val sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1")
        sslContext.init(null, null, null)
        return sslContext
    }

    /**
     * [CWE-939] Improper Authorization in Handler for Custom URL Scheme
     * Deep Link에서 인증 없이 민감한 작업 수행
     */
    fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        val action = uri.getQueryParameter("action")
        val data = uri.getQueryParameter("data")

        // 인증/권한 확인 없이 바로 실행 - 취약점!
        when (action) {
            "delete_all" -> {
                // 모든 데이터 삭제를 인증 없이 실행
                context.deleteDatabase("appDatabase.db")
                Log.d(TAG, "All data deleted via deep link")
            }
            "export" -> {
                // 데이터 내보내기를 인증 없이 실행
                val file = File(context.filesDir, "export_${data}.json")
                file.writeText("exported calendar data")
            }
            "exec" -> {
                // 명령 실행 - 매우 위험!
                if (data != null) {
                    Runtime.getRuntime().exec(data)
                }
            }
        }
    }

    /**
     * [CWE-1275] Sensitive Cookie with Improper SameSite Attribute
     * WebView에서 쿠키를 안전하지 않게 관리
     */
    fun setInsecureCookies(webView: WebView, domain: String, sessionId: String) {
        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        // Secure, HttpOnly, SameSite 속성 없이 쿠키 설정 - 취약점!
        cookieManager.setCookie(domain, "session_id=$sessionId")
        cookieManager.setCookie(domain, "auth_token=abc123xyz")
    }

    /**
     * [CWE-730] ReDoS (Regular Expression Denial of Service)
     * 취약한 정규표현식 사용
     */
    fun validateEmail(email: String): Boolean {
        // 재귀적 백트래킹이 발생하는 정규표현식 - ReDoS 취약점!
        val pattern = Pattern.compile("^([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+$")
        return pattern.matcher(email).matches()
    }
}

