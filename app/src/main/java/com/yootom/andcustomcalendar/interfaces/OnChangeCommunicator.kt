package com.yootom.andcustomcalendar.interfaces

/**
 * DB을 처리하기위한 Pregenter
 * @author yootom
 * @since final update 2020 02 22
 */
interface OnChangeCommunicator<T,T2> {
    fun onResult(context : T, result: T2)
}