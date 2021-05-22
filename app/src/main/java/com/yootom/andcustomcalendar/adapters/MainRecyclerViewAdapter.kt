package com.yootom.andcustomcalendar.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yootom.andcustomcalendar.Helpers.ViewHolderHelper
import com.yootom.andcustomcalendar.MainActivity
import com.yootom.andcustomcalendar.R
import com.yootom.andcustomcalendar.databases.AppDatabase
import com.yootom.andcustomcalendar.databases.model.TBCalendar
import com.yootom.andcustomcalendar.interfaces.OnChangeCommunicator
import com.yootom.andcustomcalendar.models.CustomCalendar
import com.yootom.andcustomcalendar.utils.DatabaseHelper
import kotlinx.android.synthetic.main.custom_dialog.view.*
import kotlinx.android.synthetic.main.item_schedule.*

/**
 * Calendar을 그리고 뿌리기 위한 어답터
 * @author yootom
 * @since final update 2020 02 22
 * @param mainActivity 부모가되는 액티비티
 * @param tbCalendar Calendar 모델
 */
class MainRecyclerViewAdapter(
    private val mainActivity: MainActivity,
    private val tbCalendar: List<TBCalendar>
) : RecyclerView.Adapter<ViewHolderHelper>() {

    private val customCalendar = CustomCalendar()

    init {
        customCalendar.initCustomCalendar {
            initView(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount(): Int {
        return CustomCalendar.LOW * CustomCalendar.WEEK
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {

        //일요일 빨강색상
        if (position % CustomCalendar.WEEK == 0) holder.txt_date.setTextColor(Color.parseColor("#FF0000"))
        else holder.txt_date.setTextColor(Color.parseColor("#676D6E"))

        if (position < customCalendar.prevMonthTailOffset || position >= customCalendar.prevMonthTailOffset + customCalendar.currentMonthMaxDate) {
            holder.txt_date.alpha = 0.3f
        } else {
            holder.txt_date.alpha = 1f
            holder.layout_click.setOnClickListener {
                val mDialogView =
                    LayoutInflater.from(mainActivity).inflate(R.layout.custom_dialog, null)
                mDialogView.txt_title.text =
                    "${getDayOfTheWeek(position)}, ${customCalendar.data[position]} ${mainActivity.getMonthName()}"
                val mBuilder = AlertDialog.Builder(mainActivity).setView(mDialogView).create()
                mBuilder.show()

                mDialogView.btn_confirm.setOnClickListener {
                    DatabaseHelper(
                        mainActivity,
                        mainActivity.appDatabase
                    ).setListener(object :
                        OnChangeCommunicator<MainActivity, List<TBCalendar>> {
                        override fun onResult(context: MainActivity, result: List<TBCalendar>) {
                            mBuilder.dismiss()
                        }
                    }).writeDatabase(
                        TBCalendar(),
                        getDayOfTheWeek(position),
                        mainActivity.getMonthName(),
                        customCalendar.data[position].toString(),
                        mDialogView.edit_text.text.toString()
                    )
                }

                mBuilder.setOnDismissListener {
                    AppDatabase.destroyInstance()
                    notifyDataSetChanged()
                    mainActivity.refreshCalendar()
                }
            }
            val subTBCalendar = mutableListOf<TBCalendar>()
            var count = 0
            var innerDay: String? = "null"
            for (i in tbCalendar) {
                if (i.dayoftheweek == getDayOfTheWeek(position) && i.day == customCalendar.data[position].toString() && i.month == mainActivity.getMonthName()) {
                    if (i.day == innerDay) {
                        subTBCalendar.add(count, i)
                        count++
                    } else {
                        innerDay = i.day
                        subTBCalendar.add(count, i)
                    }
                }
            }
            holder.rv_view.adapter =
                SubRecyclerViewAdapter(
                    subTBCalendar
                )
            holder.rv_view.layoutManager = LinearLayoutManager(mainActivity)
        }
        holder.txt_date.text = customCalendar.data[position].toString()
    }

    private fun getDayOfTheWeek(value: Int): String {
        return when (value % CustomCalendar.WEEK) {
            0 -> "Sun"
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            else -> "null"
        }
    }

    fun changeToPrevMonth() {
        customCalendar.changeToPrevMonth {
            initView(it)
        }
    }

    fun changeToNextMonth() {
        customCalendar.changeToNextMonth {
            initView(it)
        }
    }

    private fun initView(calendar: java.util.Calendar) {
        notifyDataSetChanged()
        mainActivity.refreshCurrentMonth(calendar)
    }
}