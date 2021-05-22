package com.yootom.andcustomcalendar.fragmnets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.yootom.andcustomcalendar.MainActivity
import com.yootom.andcustomcalendar.R
import com.yootom.andcustomcalendar.adapters.MainRecyclerViewAdapter
import com.yootom.andcustomcalendar.databases.model.TBCalendar
import com.yootom.andcustomcalendar.interfaces.OnChangeCommunicator
import com.yootom.andcustomcalendar.interfaces.OnChangeListener
import com.yootom.andcustomcalendar.interfaces.OnFragmentCommunicator
import com.yootom.andcustomcalendar.models.CustomCalendar
import com.yootom.andcustomcalendar.utils.DatabaseHelper
import kotlinx.android.synthetic.main.fragment_calendar.view.*

/**
 * viewPager에 그릴 Fragmnet
 * @author yootom
 * @since final update 2020 02 22
 */
class CalendarFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mainActivity.runOnUiThread(
            DatabaseHelper(
                mainActivity,
                mainActivity.appDatabase
            ).setListener(object :
                OnChangeCommunicator<MainActivity, List<TBCalendar>> {
            override fun onResult(context: MainActivity, result: List<TBCalendar>) {
                mainActivity.mainRecyclerViewAdapter =
                    MainRecyclerViewAdapter(
                        context,
                        result
                    )
                mainActivity.mainRecyclerViewAdapter.notifyDataSetChanged()
                view.rv_calendar.apply {
                    mainActivity.runOnUiThread {
                        layoutManager = GridLayoutManager(context, CustomCalendar.WEEK)
                        adapter = mainActivity.mainRecyclerViewAdapter
                        addItemDecoration(
                            DividerItemDecoration(
                                context,
                                DividerItemDecoration.VERTICAL
                            )
                        )
                        setHasFixedSize(true)
                    }
                }
                communicator.onCallback(
                    mainActivity.mainRecyclerViewAdapter)
                listener.onChanged()
            }
        })::handleDatabase)
    }

    companion object {
        private lateinit var mainActivity: MainActivity
        private lateinit var listener: OnChangeListener
        private lateinit var communicator: OnFragmentCommunicator
        fun newInstance(fm: FragmentActivity) = CalendarFragment()
            .apply {
            if (fm is MainActivity) {
                mainActivity = fm
                listener = fm
                communicator = fm
            }
        }
    }
}