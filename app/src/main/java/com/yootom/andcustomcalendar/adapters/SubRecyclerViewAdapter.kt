package com.yootom.andcustomcalendar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yootom.andcustomcalendar.Helpers.ViewHolderHelper
import com.yootom.andcustomcalendar.R
import com.yootom.andcustomcalendar.databases.model.TBCalendar
import kotlinx.android.synthetic.main.item_calendar.*

/**
* 그려진 Calendar에 일정을 그리기 위한 어답터
* @author yootom
* @since final update 2020 02 22
* @param tbCalendar Calendar 모델
*/
class SubRecyclerViewAdapter(
    private val tbCalendar: List<TBCalendar>
) :
    RecyclerView.Adapter<ViewHolderHelper>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount(): Int {
        return tbCalendar.size
    }

    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {
        holder.tv_calendar.text = tbCalendar[position].description
    }
}