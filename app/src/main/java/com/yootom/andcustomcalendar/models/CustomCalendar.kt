package com.yootom.andcustomcalendar.models

import java.util.*

/**
 * Calendar model
 * @author yootom
 * @since final update 2020 02 22
 */
class CustomCalendar {

    companion object {
        const val WEEK = 7
        const val LOW = 6
    }

    private val mCalendar = Calendar.getInstance()

    var prevMonthTailOffset = 0
    var nextMonthHeadOffset = 0
    var currentMonthMaxDate = 0

    var data = arrayListOf<Int>()

    init {
        mCalendar.time = Date()
    }

    fun initCustomCalendar(callback: (Calendar) -> Unit) {
        setDate(callback)
    }

    fun changeToPrevMonth(callback: (Calendar) -> Unit) {
        if (mCalendar.get(Calendar.MONTH) == 0) {
            mCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR) - 1)
            mCalendar.set(Calendar.MONTH, Calendar.DECEMBER)
        } else {
            mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1)
        }
        setDate(callback)
    }

    fun changeToNextMonth(callback: (Calendar) -> Unit) {
        if (mCalendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            mCalendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR) + 1)
            mCalendar.set(Calendar.MONTH, 0)
        } else {
            mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1)
        }
        setDate(callback)
    }

    private fun setDate(callback: (Calendar) -> Unit) {

        data.clear()

        mCalendar.set(Calendar.DATE, 1)

        currentMonthMaxDate = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        prevMonthTailOffset = mCalendar.get(Calendar.DAY_OF_WEEK) - 1

        setPrevMonthTail(mCalendar.clone() as Calendar)
        setCurrentMonth(mCalendar)

        nextMonthHeadOffset =
            LOW * WEEK - (prevMonthTailOffset + currentMonthMaxDate)
        setNextMonthHead()

        callback(mCalendar)
    }

    private fun setPrevMonthTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        var maxOffsetDate = maxDate - prevMonthTailOffset

        for (i in 1..prevMonthTailOffset) data.add(++maxOffsetDate)
    }

    private fun setCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) data.add(i)
    }

    private fun setNextMonthHead() {
        var date = 1
        for (i in 1..nextMonthHeadOffset) data.add(date++)
    }

    fun getTimeInMillis(): Long {
        return mCalendar.timeInMillis
    }

    fun setTimeInMillis(long: Long): Calendar {
        mCalendar.timeInMillis = long
        return mCalendar
    }
}