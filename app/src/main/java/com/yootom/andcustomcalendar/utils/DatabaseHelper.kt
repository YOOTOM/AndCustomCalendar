package com.yootom.andcustomcalendar.utils

import com.yootom.andcustomcalendar.MainActivity
import com.yootom.andcustomcalendar.databases.AppDatabase
import com.yootom.andcustomcalendar.databases.model.TBCalendar
import com.yootom.andcustomcalendar.interfaces.OnChangeCommunicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DB Helper
 * @author yootom
 * @since final update 2020 02 22
 * @param mainActivity 작업할 위치
 * @param appDatabase
 */
class DatabaseHelper(
    private val mainActivity: MainActivity,
    private var appDatabase: AppDatabase?
) {
    private var onChangeCommunicator: OnChangeCommunicator<MainActivity, List<TBCalendar>>? = null

    fun setListener(onChangeCommunicator: OnChangeCommunicator<MainActivity, List<TBCalendar>>?): DatabaseHelper {
        this.onChangeCommunicator = onChangeCommunicator
        return this
    }

    fun handleDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            onChangeCommunicator?.onResult(mainActivity, appDatabase?.tbCalendarDao()?.getAll()!!)
        }
    }

    fun writeDatabase(
        newTBCalendar: TBCalendar,
        dayoftheweek: String,
        month: String,
        day: String,
        description: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            newTBCalendar.dayoftheweek = dayoftheweek
            newTBCalendar.month = month
            newTBCalendar.day = day
            newTBCalendar.description = description
            mainActivity.appDatabase?.tbCalendarDao()?.apply {
                insert(newTBCalendar)
            }
            onChangeCommunicator?.onResult(mainActivity, appDatabase?.tbCalendarDao()?.getAll()!!)
        }
    }

    fun clearDatabase() {
        mainActivity.appDatabase?.tbCalendarDao()?.apply {
            deleteALL()
        }

    }
}