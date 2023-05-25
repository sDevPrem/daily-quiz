package com.sdevprem.dailyquiz.uitls

import android.content.Context
import com.sdevprem.dailyquiz.R
import java.util.Calendar
import java.util.Date

object DateUtils {
    fun convertStringMthToCalendarMth(mth: String, context: Context) = when (mth) {
        context.getString(R.string.mth_jan) -> Calendar.JANUARY
        context.getString(R.string.mth_feb) -> Calendar.FEBRUARY
        context.getString(R.string.mth_mar) -> Calendar.MARCH
        context.getString(R.string.mth_apr) -> Calendar.APRIL
        context.getString(R.string.mth_may) -> Calendar.MAY
        context.getString(R.string.mth_jun) -> Calendar.JUNE
        context.getString(R.string.mth_jul) -> Calendar.JULY
        context.getString(R.string.mth_aug) -> Calendar.AUGUST
        context.getString(R.string.mth_sep) -> Calendar.SEPTEMBER
        context.getString(R.string.mth_oct) -> Calendar.OCTOBER
        context.getString(R.string.mth_nov) -> Calendar.NOVEMBER
        context.getString(R.string.mth_dec) -> Calendar.DECEMBER
        else -> -1
    }

    fun Calendar.getStringMth() = when (get(Calendar.MONTH)) {
        Calendar.JANUARY -> R.string.mth_jan
        Calendar.FEBRUARY -> R.string.mth_feb
        Calendar.MARCH -> R.string.mth_mar
        Calendar.APRIL -> R.string.mth_apr
        Calendar.MAY -> R.string.mth_may
        Calendar.JUNE -> R.string.mth_jun
        Calendar.JULY -> R.string.mth_jul
        Calendar.AUGUST -> R.string.mth_aug
        Calendar.SEPTEMBER -> R.string.mth_sep
        Calendar.OCTOBER -> R.string.mth_oct
        Calendar.NOVEMBER -> R.string.mth_nov
        Calendar.DECEMBER -> R.string.mth_dec
        else -> -1
    }

    fun Calendar.setMaximumTimeOfMth() = this.apply {
        set(
            get(Calendar.YEAR),
            get(Calendar.MONTH),
            getActualMaximum(Calendar.DATE),
            getActualMaximum(Calendar.HOUR_OF_DAY),
            getActualMaximum(Calendar.MINUTE),
            getActualMaximum(Calendar.SECOND),
        )
    }

    fun Calendar.setMinimumTimeOfMth() = this.apply {
        set(
            get(Calendar.YEAR),
            get(Calendar.MONTH),
            getActualMinimum(Calendar.DATE),
            getActualMinimum(Calendar.HOUR_OF_DAY),
            getActualMinimum(Calendar.MINUTE),
            getActualMinimum(Calendar.SECOND),
        )
    }

    fun Date.toCalendar() = Calendar.getInstance().apply {
        time = this@toCalendar
    }
}