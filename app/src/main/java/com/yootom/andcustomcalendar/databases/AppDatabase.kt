package com.yootom.andcustomcalendar.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yootom.andcustomcalendar.databases.dao.TBCalendarDAO
import com.yootom.andcustomcalendar.databases.model.TBCalendar

/**
 * Room을 이용하여 DB 생성 및 초기화
 * @author yootom
 * @since final update 2020 02 22
 */
@Database(entities = [TBCalendar::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tbCalendarDao() : TBCalendarDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "appDatabase.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
