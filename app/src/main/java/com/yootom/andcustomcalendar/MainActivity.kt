package com.yootom.andcustomcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.yootom.andcustomcalendar.adapters.CalendarAdapter
import com.yootom.andcustomcalendar.adapters.MainRecyclerViewAdapter
import com.yootom.andcustomcalendar.databases.AppDatabase
import com.yootom.andcustomcalendar.interfaces.OnChangeListener
import com.yootom.andcustomcalendar.interfaces.OnFragmentCommunicator
import com.yootom.andcustomcalendar.interfaces.OnViewPagerChangeListener
import com.yootom.andcustomcalendar.models.Dates
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 메인 액티비티
 * @author yootom
 * @since final update 2020 02 22
 */

class MainActivity : AppCompatActivity(),
    OnChangeListener,
    OnFragmentCommunicator,
    OnViewPagerChangeListener {
    lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter
    var appDatabase: AppDatabase? = null

    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        appDatabase = AppDatabase.getInstance(this)
        calendarAdapter = CalendarAdapter(this)

        calendar.adapter = calendarAdapter
        calendar.setCurrentItem(CalendarAdapter.START_POS, false)
        calendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })

        txt_prev_month.setOnClickListener {
            mainRecyclerViewAdapter.changeToPrevMonth()
        }

        txt_next_month.setOnClickListener {
            mainRecyclerViewAdapter.changeToNextMonth()
        }
    }

    private lateinit var dates: Dates

    fun refreshCurrentMonth(calendar: Calendar) {
        val month = SimpleDateFormat("MM", Locale.KOREAN)
        val monthNum = SimpleDateFormat("M", Locale.KOREAN)
        val year = SimpleDateFormat("yyyy", Locale.KOREAN)
        dates = Dates(
            month.format(calendar.time),
            year.format(calendar.time),
            monthNames[monthNum.format(calendar.time).toInt() - 1]
        )
        runOnUiThread {
            txt_current_month.text = "${dates.Year} ${dates.Month}"
        }
    }

    fun refreshCalendar() {
        initView()
    }

    fun getMonth(): String {
        return dates.Month
    }

    fun getYear(): String {
        return dates.Year
    }

    fun getMonthName(): String {
        return dates.MonthNum
    }

    private var monthNames: ArrayList<String> = arrayListOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    override fun onDestroy() {
        super.onDestroy()
        AppDatabase.destroyInstance()
    }

    override fun onChanged() {
        Log.d(MainActivity::class.java.simpleName, "PagerChanged!!!!!!!!!!!!!!!!")
    }

    override fun onCallback(any: Any) {
    }

    override fun OnChangeToNextMonth() {
    }

    override fun OnChangeToPrevMonth() {
    }
}