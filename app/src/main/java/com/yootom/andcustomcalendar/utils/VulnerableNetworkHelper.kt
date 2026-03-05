package com.yootom.andcustomcalendar.utils

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 네트워크 유틸리티 (일정 동기화 용)
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 */
class VulnerableNetworkHelper(private val context: Context) {

    companion object {
        private const val TAG = "NetworkHelper"
        // [CWE-798] Hardcoded Credentials - 하드코딩된 API 키와 비밀번호
        private const val API_KEY = "sk-proj-ABCDEfghij1234567890secretKeyValue"
        private const val API_SECRET = "super_secret_password_12345"
        private const val DB_PASSWORD = "admin123!"
        private const val ENCRYPTION_KEY = "AES256-KEY-HARDCODED-NEVER-DO-THIS"
    }

    /**
     * [CWE-295] Improper Certificate Validation
     * SSL 인증서 검증을 완전히 비활성화하는 TrustManager
     */
    fun createInsecureConnection(urlString: String): HttpsURLConnection {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // 모든 클라이언트 인증서를 신뢰 - 취약점!
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // 모든 서버 인증서를 신뢰 - 취약점!
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val url = URL(urlString)
        val connection = url.openConnection() as HttpsURLConnection
        connection.sslSocketFactory = sslContext.socketFactory
        // [CWE-295] HostnameVerifier도 비활성화
        connection.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }

        return connection
    }

    /**
     * [CWE-319] Cleartext Transmission of Sensitive Information
     * 민감한 데이터를 HTTP(비암호화)로 전송
     */
    fun syncCalendarData(userId: String, password: String): String? {
        // HTTP를 사용하여 비밀번호를 평문으로 전송 - 취약점!
        val url = URL("http://api.calendar-sync.example.com/login?user=$userId&pass=$password&apikey=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readLine()
        reader.close()
        return response
    }

    /**
     * [CWE-312] Cleartext Storage of Sensitive Information
     * 민감 정보를 SharedPreferences에 평문으로 저장
     */
    fun saveCredentials(username: String, password: String, token: String) {
        val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)       // 비밀번호를 평문 저장 - 취약점!
            putString("auth_token", token)         // 토큰을 평문 저장 - 취약점!
            putString("api_key", API_KEY)
            apply()
        }
        // [CWE-532] Log에 민감 정보 출력
        Log.d(TAG, "User logged in: username=$username, password=$password, token=$token")
        Log.i(TAG, "API Key used: $API_KEY")
    }

    /**
     * [CWE-079] 사용자 입력을 그대로 WebView에 반영 (XSS)
     * [CWE-94] Code Injection
     */
    fun generateCalendarHtml(userInput: String): String {
        // 사용자 입력을 이스케이프 없이 HTML에 삽입 - XSS 취약점!
        return """
            <html>
            <body>
                <h1>Calendar Event</h1>
                <div>$userInput</div>
                <script>
                    document.title = "$userInput";
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * [CWE-089] SQL Injection
     * 사용자 입력을 직접 SQL 쿼리에 삽입
     */
    fun searchEventsRaw(context: Context, searchTerm: String): List<String> {
        val results = mutableListOf<String>()
        val db = context.openOrCreateDatabase("calendar_search.db", Context.MODE_PRIVATE, null)

        // 사용자 입력을 직접 쿼리에 삽입 - SQL Injection 취약점!
        val query = "SELECT * FROM events WHERE title LIKE '%$searchTerm%' OR description LIKE '%$searchTerm%'"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            results.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        return results
    }

    /**
     * [CWE-089] SQL Injection - 또 다른 패턴
     */
    fun deleteEventByName(context: Context, eventName: String) {
        val db = context.openOrCreateDatabase("calendar_search.db", Context.MODE_PRIVATE, null)
        // String concatenation으로 SQL 인젝션 취약
        db.execSQL("DELETE FROM events WHERE name = '$eventName'")
        db.close()
    }
}

