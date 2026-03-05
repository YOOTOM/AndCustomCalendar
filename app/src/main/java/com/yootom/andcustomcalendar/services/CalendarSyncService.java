package com.yootom.andcustomcalendar.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * [CWE-926] Exported Service - 권한 보호 없이 외부에 노출
 * [CWE-319] Cleartext HTTP 통신
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 */
public class CalendarSyncService extends Service {

    private static final String TAG = "CalendarSyncService";

    // [CWE-798] Hardcoded credentials
    private static final String SYNC_SERVER = "http://sync.calendar-api.example.com";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.hardcoded_token";
    private static final String SYNC_PASSWORD = "SyncP@ssw0rd2024!";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        // [CWE-925] Intent 발신자를 검증하지 않음
        String action = intent.getStringExtra("sync_action");
        String userData = intent.getStringExtra("user_data");
        String targetUrl = intent.getStringExtra("target_url");

        Log.d(TAG, "Sync started - action: " + action + ", data: " + userData);

        switch (action != null ? action : "") {
            case "sync_upload":
                // [CWE-319] HTTP로 데이터 전송
                uploadData(userData);
                break;
            case "sync_download":
                downloadData();
                break;
            case "custom_request":
                // [CWE-918] SSRF (Server-Side Request Forgery)
                // 사용자가 제공한 URL로 요청 - SSRF 취약점
                if (targetUrl != null) {
                    makeRequest(targetUrl, userData);
                }
                break;
        }

        return START_NOT_STICKY;
    }

    /**
     * [CWE-319] HTTP 평문 통신으로 데이터 전송
     * [CWE-532] 민감 정보 로깅
     */
    private void uploadData(String data) {
        try {
            // 취약: HTTP 사용 (HTTPS 아님)
            URL url = new URL(SYNC_SERVER + "/api/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", AUTH_TOKEN);
            conn.setRequestProperty("X-Sync-Password", SYNC_PASSWORD);
            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data != null ? data : "");
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Upload response: " + responseCode + ", auth: " + AUTH_TOKEN);

            conn.disconnect();
        } catch (Exception e) {
            // [CWE-209] 상세 에러 정보 노출
            Log.e(TAG, "Upload failed: " + e.toString() + "\n" + Log.getStackTraceString(e));
        }
    }

    private void downloadData() {
        try {
            URL url = new URL(SYNC_SERVER + "/api/download?token=" + AUTH_TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            Log.d(TAG, "Downloaded data: " + response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Download failed", e);
        }
    }

    /**
     * [CWE-918] Server-Side Request Forgery (SSRF)
     * 사용자 제공 URL로 요청을 보냄
     */
    private void makeRequest(String targetUrl, String data) {
        try {
            // 취약: URL을 검증하지 않고 그대로 사용
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data != null ? data : "");
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            reader.close();

            Log.d(TAG, "Custom request to " + targetUrl + " response: " + response);
        } catch (Exception e) {
            Log.e(TAG, "Request to " + targetUrl + " failed: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

