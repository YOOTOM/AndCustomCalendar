package com.yootom.andcustomcalendar.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yootom.andcustomcalendar.databases.model.TBCalendar

/**
 * Calendar 모델과 Room을 이용하여 DB(SQLite)을 쓰기위한 도구
 * @author yootom
 * @since final update 2020 02 22
 */
@Dao
interface TBCalendarDAO {
    @Query("SELECT * FROM tbCalendar")
    fun getAll(): List<TBCalendar>

    @Insert
    fun insert(vararg tbcontacks : TBCalendar)

    @Query("DELETE from tbCalendar")
    fun deleteALL()
}