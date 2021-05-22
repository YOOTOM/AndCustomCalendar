package com.yootom.andcustomcalendar.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yootom.andcustomcalendar.fragmnets.CalendarFragment

/**
 * ViewPager 위에 Fragment 뷰를 뿌리기위한 어답터
 * @author yootom
 * @since final update 2020 02 22
 * @param fm 부모되는 액티비티
 */
class CalendarAdapter(private val fm: FragmentActivity) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): CalendarFragment {
        //Log.d(CalendarFragment::class.java.simpleName, "position : $position")
        return CalendarFragment.newInstance(fm)
    }

    companion object {
        const val START_POS = Int.MAX_VALUE / 2
    }
}