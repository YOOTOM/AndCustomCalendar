package com.yootom.andcustomcalendar.Helpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

/**
 * 뷰홀더 클래스 생성
 * 이 클래스 내부에 뷰 인스턴스를 캐시할 때 필요한 객체를 추가
 * @author yootom
 * @since final update 2020 02 22
 * @param containerView
 */
open class ViewHolderHelper(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer