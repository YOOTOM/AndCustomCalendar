package com.yootom.andcustomcalendar.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * [CWE-926] Exported ContentProvider - 권한 보호 없이 외부에 노출
 * [CWE-089] SQL Injection in ContentProvider
 * ⚠️ 의도적으로 보안 취약점이 포함된 코드입니다 (CodeQL 테스트용)
 *
 * 외부 앱이 이 Provider를 통해 데이터에 무단 접근/수정 가능
 */
public class CalendarContentProvider extends ContentProvider {

    private static final String TAG = "CalendarProvider";
    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        // 취약: MODE_PRIVATE이 아닌 DB 생성
        database = getContext().openOrCreateDatabase(
                "calendar_provider.db", android.content.Context.MODE_PRIVATE, null);
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS calendar_events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "description TEXT," +
                        "date TEXT," +
                        "user_token TEXT" +  // 민감 정보를 평문으로 저장
                        ")"
        );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // [CWE-089] SQL Injection - selection 파라미터를 검증 없이 사용
        Log.d(TAG, "Query with selection: " + selection);

        // 취약: 외부 입력(selection)을 직접 쿼리에 사용
        String query = "SELECT * FROM calendar_events WHERE " + selection;
        return database.rawQuery(query, null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 권한 검증 없이 삽입 허용
        long id = database.insert("calendar_events", null, values);
        Log.d(TAG, "Inserted row: " + id + ", values: " + values.toString());
        return Uri.parse("content://com.yootom.andcustomcalendar.provider/events/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // [CWE-089] SQL Injection in delete
        // 취약: selection을 직접 사용
        String deleteQuery = "DELETE FROM calendar_events WHERE " + selection;
        database.execSQL(deleteQuery);
        Log.d(TAG, "Deleted with selection: " + selection);
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // 권한 검증 없이 업데이트 허용
        return database.update("calendar_events", values, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.yootom.calendar_events";
    }
}

