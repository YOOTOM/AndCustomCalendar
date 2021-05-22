package com.yootom.andcustomcalendar.databases.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Calendar 모델과 Room을 이용하여 Table 생성
 * @author yootom
 * @since final update 2020 02 22
 * @param id 순번,기본키, 자동생성
 * @param dayoftheweek 요일
 * @param month 달
 * @param day 일수
 * @param description 일정내용
 */
@Entity(tableName = "tbCalendar")
class TBCalendar(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "dayoftheweek") var dayoftheweek: String?,
    @ColumnInfo(name = "month") var month: String?,
    @ColumnInfo(name = "day") var day: String?,
    @ColumnInfo(name = "description") var description: String?
) {
    constructor() : this(null, "", "", "", "")
}