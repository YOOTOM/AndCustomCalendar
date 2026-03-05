package com.yootom.andcustomcalendar.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 파일/데이터 처리 유틸리티
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 */
class VulnerableFileHelper(private val context: Context) {

    companion object {
        private const val TAG = "FileHelper"
    }

    /**
     * [CWE-022] Path Traversal
     * 사용자 입력을 파일 경로에 직접 사용
     */
    fun readCalendarExport(fileName: String): String {
        // 사용자 입력을 그대로 파일 경로에 사용 - Path Traversal 취약점!
        // "../../../etc/passwd" 같은 입력으로 임의 파일 읽기 가능
        val file = File(context.filesDir, fileName)
        return file.readText()
    }

    /**
     * [CWE-022] Path Traversal - 외부 저장소
     */
    fun saveExportFile(userProvidedPath: String, content: String) {
        // 외부 저장소에 사용자가 지정한 경로로 저장 - Path Traversal 취약점!
        val baseDir = Environment.getExternalStorageDirectory()
        val file = File(baseDir, userProvidedPath)
        file.writeText(content)
        Log.d(TAG, "File saved to: ${file.absolutePath}")
    }

    /**
     * [CWE-611] XXE (XML External Entity) Injection
     * 외부 엔티티 처리가 활성화된 XML 파서 사용
     */
    fun parseCalendarXml(xmlInput: InputStream): List<String> {
        val events = mutableListOf<String>()
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        // XXE 방지 설정을 하지 않음 - 취약점!
        // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) // 이걸 해야 안전
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xmlInput)

        val nodeList = document.getElementsByTagName("event")
        for (i in 0 until nodeList.length) {
            events.add(nodeList.item(i).textContent)
        }
        return events
    }

    /**
     * [CWE-502] Deserialization of Untrusted Data
     * 신뢰할 수 없는 소스에서 역직렬화
     */
    fun loadCalendarBackup(backupFile: File): Any? {
        // 외부 파일을 직접 역직렬화 - 위험!
        val fis = FileInputStream(backupFile)
        val ois = ObjectInputStream(fis)
        val data = ois.readObject()  // Deserialization 취약점!
        ois.close()
        fis.close()
        return data
    }

    /**
     * [CWE-400] Uncontrolled Resource Consumption
     * 파일 크기 제한 없이 읽기
     */
    fun importLargeCalendarFile(inputStream: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var bytesRead: Int
        // 파일 크기를 확인하지 않고 무한 읽기 - DoS 가능
        while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
            buffer.write(data, 0, bytesRead)
        }
        buffer.flush()
        return buffer.toByteArray()
    }

    /**
     * [CWE-327] Use of Broken Crypto Algorithm
     * 취약한 해시 알고리즘 사용
     */
    fun hashPassword(password: String): String {
        // MD5는 취약한 해시 알고리즘 - 취약점!
        val digest = java.security.MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * [CWE-327] Use of Broken Crypto - SHA1도 취약
     */
    fun generateChecksum(data: String): String {
        // SHA-1은 충돌 공격에 취약
        val digest = java.security.MessageDigest.getInstance("SHA-1")
        val hashBytes = digest.digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * [CWE-329] Not Using Unpredictable IV with CBC
     * [CWE-327] 안전하지 않은 암호화 모드 사용 (ECB)
     */
    fun encryptData(data: String, key: String): ByteArray {
        // DES는 취약한 암호화 알고리즘, ECB 모드는 안전하지 않음
        val cipher = javax.crypto.Cipher.getInstance("DES/ECB/PKCS5Padding")
        val keySpec = javax.crypto.spec.SecretKeySpec(key.toByteArray().copyOf(8), "DES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec)
        return cipher.doFinal(data.toByteArray())
    }

    /**
     * [CWE-338] Use of Cryptographically Weak PRNG
     * 예측 가능한 난수 생성기 사용
     */
    fun generateSessionToken(): String {
        // java.util.Random은 암호학적으로 안전하지 않음 - 취약점!
        val random = java.util.Random()
        val token = StringBuilder()
        for (i in 0..31) {
            token.append(Integer.toHexString(random.nextInt(16)))
        }
        return token.toString()
    }

    /**
     * [CWE-073] External Control of File Name or Path
     * [CWE-022] Zip Slip vulnerability
     */
    fun extractZipFile(zipFile: File, destDir: File) {
        val zis = ZipInputStream(FileInputStream(zipFile))
        var entry: ZipEntry? = zis.nextEntry

        while (entry != null) {
            // entry.name을 검증하지 않고 사용 - Zip Slip 취약점!
            // "../../../malicious" 같은 경로로 임의 위치에 파일 생성 가능
            val newFile = File(destDir, entry.name)
            if (entry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                val fos = FileOutputStream(newFile)
                val buffer = ByteArray(1024)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
            }
            entry = zis.nextEntry
        }
        zis.closeEntry()
        zis.close()
    }

    /**
     * [CWE-532] Insertion of Sensitive Info into Log File
     * 민감 정보를 로그에 출력
     */
    fun processUserLogin(email: String, password: String, creditCard: String): Boolean {
        Log.d(TAG, "Login attempt - email: $email, password: $password")
        Log.i(TAG, "Credit card: $creditCard")
        Log.w(TAG, "Processing payment with card: $creditCard")

        // 로그 파일에도 직접 기록
        val logFile = File(context.filesDir, "app_debug.log")
        logFile.appendText("${java.util.Date()}: Login $email / $password / CC: $creditCard\n")

        return true
    }
}

