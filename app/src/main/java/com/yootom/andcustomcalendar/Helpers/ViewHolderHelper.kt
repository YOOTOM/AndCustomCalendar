package com.yootom.andcustomcalendar.Helpers

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.yootom.andcustomcalendar.R

/**
 * 뷰홀더 클래스 생성
 * 이 클래스 내부에 뷰 인스턴스를 캐시할 때 필요한 객체를 추가
 * @author yootom
 * @since final update 2020 02 22
 * @param itemView
 */
open class ViewHolderHelper(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // item_schedule.xml
    val txt_date: AppCompatTextView by lazy { itemView.findViewById(R.id.txt_date) }
    val rv_view: RecyclerView by lazy { itemView.findViewById(R.id.rv_view) }
    val layout_click: LinearLayout by lazy { itemView.findViewById(R.id.layout_click) }

    // item_calendar.xml
    val tv_calendar: AppCompatTextView by lazy { itemView.findViewById(R.id.tv_calendar) }
}
