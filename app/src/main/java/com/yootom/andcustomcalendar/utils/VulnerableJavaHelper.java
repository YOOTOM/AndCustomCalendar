package com.yootom.andcustomcalendar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Java로 작성된 취약점 코드 모음
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 *
 * CodeQL은 Java 소스에서 더 넓은 범위의 취약점을 탐지합니다.
 */
public class VulnerableJavaHelper {

    private static final String TAG = "VulnerableJavaHelper";

    // =====================================================
    // [CWE-798] Hard-coded Credentials
    // =====================================================
    private static final String SECRET_KEY = "MyS3cr3tK3y!@#$%";
    private static final String DB_PASSWORD = "root_password_123";
    private static final String API_TOKEN = "ghp_ABCDefghijklmnopqrstuvwxyz1234567890";
    private static final String AWS_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private final Context context;

    public VulnerableJavaHelper(Context context) {
        this.context = context;
    }

    // =====================================================
    // [CWE-089] SQL Injection
    // =====================================================

    /**
     * 사용자 입력을 직접 SQL 쿼리에 삽입 - SQL Injection
     */
    public List<String> searchEvents(String userInput) {
        List<String> results = new ArrayList<>();
        SQLiteDatabase db = context.openOrCreateDatabase("events.db", Context.MODE_PRIVATE, null);

        // 취약: String concatenation으로 쿼리 생성
        String query = "SELECT title FROM events WHERE title = '" + userInput + "'";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            results.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return results;
    }

    /**
     * 또 다른 SQL Injection 패턴 - execSQL
     */
    public void updateEventDescription(String eventId, String newDescription) {
        SQLiteDatabase db = context.openOrCreateDatabase("events.db", Context.MODE_PRIVATE, null);
        // 취약: 사용자 입력으로 직접 SQL 실행
        db.execSQL("UPDATE events SET description = '" + newDescription + "' WHERE id = " + eventId);
        db.close();
    }

    // =====================================================
    // [CWE-078] OS Command Injection
    // =====================================================

    /**
     * 사용자 입력을 시스템 명령에 직접 전달
     */
    public String executeSystemCommand(String userCommand) throws IOException {
        // 취약: 사용자 입력을 검증 없이 exec()에 전달
        Process process = Runtime.getRuntime().exec(userCommand);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    /**
     * 또 다른 Command Injection 패턴
     */
    public String lookupHost(String hostname) throws IOException {
        // 취약: 사용자 입력이 shell 명령에 포함됨
        String[] cmd = {"/bin/sh", "-c", "nslookup " + hostname};
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }

    // =====================================================
    // [CWE-295] Improper Certificate Validation
    // =====================================================

    /**
     * SSL 인증서 검증을 완전히 비활성화
     */
    public HttpsURLConnection createUnsafeConnection(String urlString) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // 취약: 모든 인증서 신뢰
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // 취약: 모든 인증서 신뢰
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection connection = (HttpsURLConnection) new URL(urlString).openConnection();
        connection.setSSLSocketFactory(sc.getSocketFactory());

        // 취약: HostnameVerifier도 비활성화
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;  // 모든 호스트네임 허용
            }
        });

        return connection;
    }

    // =====================================================
    // [CWE-319] Cleartext Transmission
    // =====================================================

    /**
     * HTTP로 민감한 데이터 전송
     */
    public String sendCredentialsOverHttp(String username, String password) throws IOException {
        // 취약: HTTPS가 아닌 HTTP 사용
        URL url = new URL("http://api.example.com/auth?user=" + username + "&pass=" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(
                (username + ":" + password).getBytes(), Base64.NO_WRAP));

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = reader.readLine();
        reader.close();

        // 취약: 응답에 포함된 토큰을 로그에 출력
        Log.d(TAG, "Auth response with credentials: " + username + ":" + password + " -> " + response);
        return response;
    }

    // =====================================================
    // [CWE-327] Use of Broken Cryptographic Algorithm
    // =====================================================

    /**
     * MD5로 비밀번호 해싱 - 취약한 알고리즘
     */
    public String hashWithMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * DES 암호화 - 취약한 알고리즘
     */
    public byte[] encryptWithDES(String plaintext, String key) throws Exception {
        // 취약: DES는 56비트 키로 더 이상 안전하지 않음
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(plaintext.getBytes());
    }

    /**
     * ECB 모드 AES - 패턴이 보존되는 취약한 모드
     */
    public byte[] encryptWithAESECB(String plaintext) throws Exception {
        // 취약: ECB 모드는 동일한 평문 블록에 동일한 암호문을 생성
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(plaintext.getBytes());
    }

    // =====================================================
    // [CWE-338] Weak Random Number Generator
    // =====================================================

    /**
     * 예측 가능한 난수로 보안 토큰 생성
     */
    public String generateInsecureToken() {
        // 취약: java.util.Random은 시드를 예측할 수 있음
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            token.append(Integer.toHexString(random.nextInt(16)));
        }
        return token.toString();
    }

    /**
     * 고정 시드를 사용하는 난수 생성
     */
    public int generatePredictableNumber() {
        // 취약: 고정 시드로 항상 같은 난수 시퀀스 생성
        Random random = new Random(12345L);
        return random.nextInt(1000000);
    }

    // =====================================================
    // [CWE-022] Path Traversal
    // =====================================================

    /**
     * 사용자 입력을 파일 경로에 직접 사용
     */
    public String readUserFile(String fileName) throws IOException {
        // 취약: "../../../etc/passwd" 같은 입력으로 임의 파일 접근 가능
        File file = new File(context.getFilesDir(), fileName);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    // =====================================================
    // [CWE-502] Deserialization of Untrusted Data
    // =====================================================

    /**
     * 신뢰할 수 없는 데이터를 역직렬화
     */
    public Object deserializeData(InputStream input) throws Exception {
        // 취약: 입력 스트림을 검증 없이 역직렬화
        ObjectInputStream ois = new ObjectInputStream(input);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    // =====================================================
    // [CWE-532] Information Exposure Through Log Files
    // =====================================================

    /**
     * 민감 정보를 로그에 출력
     */
    public void logSensitiveData(String ssn, String creditCard, String password) {
        // 취약: 모든 민감 정보가 로그에 기록됨
        Log.d(TAG, "SSN: " + ssn);
        Log.d(TAG, "Credit Card: " + creditCard);
        Log.d(TAG, "Password: " + password);
        Log.i(TAG, "Secret Key: " + SECRET_KEY);
        Log.w(TAG, "AWS Secret: " + AWS_SECRET);
    }

    // =====================================================
    // [CWE-312] Cleartext Storage of Sensitive Information
    // =====================================================

    /**
     * SharedPreferences에 민감 정보를 평문 저장
     */
    public void storeCredentialsInsecurely(String username, String password, String token) {
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);      // 취약: 비밀번호 평문 저장
        editor.putString("access_token", token);     // 취약: 토큰 평문 저장
        editor.putString("api_key", API_TOKEN);      // 취약: API 키 평문 저장
        editor.apply();
    }

    // =====================================================
    // [CWE-200] Exposure of Sensitive Information
    // =====================================================

    /**
     * 에러 메시지에서 민감한 시스템 정보 노출
     */
    public String processRequest(String data) {
        try {
            int value = Integer.parseInt(data);
            return "Processed: " + value;
        } catch (NumberFormatException e) {
            // 취약: 상세한 에러 정보를 반환
            return "Error: " + e.toString() + "\n" +
                    "Stack: " + Log.getStackTraceString(e) + "\n" +
                    "DB Password: " + DB_PASSWORD + "\n" +
                    "System Info: " + System.getProperty("os.name") + " " + System.getProperty("os.version");
        }
    }

    // =====================================================
    // [CWE-676] Use of Potentially Dangerous Function
    // [CWE-134] Use of Externally-Controlled Format String
    // =====================================================

    /**
     * 사용자 입력을 포맷 문자열에 직접 사용
     */
    public String formatUserData(String userInput) {
        // 취약: 사용자 입력을 포맷 문자열로 사용
        return String.format(userInput, "calendar_data");
    }

    // =====================================================
    // [CWE-611] XXE - XML External Entity Injection
    // =====================================================

    /**
     * 외부 엔티티가 활성화된 XML 파서
     */
    public String parseXmlUnsafe(InputStream xmlInput) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
        // 취약: XXE 방지 설정 없음
        // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(xmlInput);
        return doc.getDocumentElement().getTextContent();
    }

    // =====================================================
    // [CWE-079] Cross-Site Scripting (XSS) via WebView
    // =====================================================

    /**
     * 사용자 입력을 HTML에 이스케이프 없이 삽입
     */
    public String createHtmlContent(String userTitle, String userBody) {
        // 취약: 사용자 입력을 이스케이프 없이 HTML에 삽입
        return "<html><head><title>" + userTitle + "</title></head>" +
                "<body><h1>" + userTitle + "</h1>" +
                "<div>" + userBody + "</div>" +
                "<script>var data = '" + userBody + "';</script>" +
                "</body></html>";
    }

    // =====================================================
    // [CWE-377] Insecure Temporary File
    // =====================================================

    /**
     * 안전하지 않은 임시 파일 생성
     */
    public File createTempFile(String content) throws IOException {
        // 취약: 예측 가능한 파일명으로 임시 파일 생성
        File tempFile = new File(context.getCacheDir(), "temp_calendar_" + System.currentTimeMillis() + ".tmp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(content.getBytes());
        fos.close();
        // 취약: world-readable 권한 설정
        tempFile.setReadable(true, false);
        tempFile.setWritable(true, false);
        return tempFile;
    }

    // =====================================================
    // [CWE-EMPTY-PASSWORD] Empty password in connection string
    // =====================================================

    /**
     * 빈 비밀번호로 데이터베이스 연결
     */
    public void connectToDatabase() {
        try {
            // 취약: 빈 비밀번호
            java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/calendar", "root", "");
        } catch (Exception e) {
            Log.e(TAG, "DB connection failed: " + e.getMessage());
        }
    }
}

