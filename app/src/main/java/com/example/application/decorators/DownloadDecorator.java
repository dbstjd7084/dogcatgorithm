package com.example.application.decorators;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

public class DownloadDecorator implements DayViewDecorator {
    private final Calendar installDay;


    @SuppressLint("UseCompatLoadingForDrawables")
    public DownloadDecorator(Calendar installDay) {
        this.installDay = installDay;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (installDay.get(Calendar.YEAR) == day.getYear() &&
        installDay.get(Calendar.MONTH) + 1 == day.getMonth() &&
        installDay.get(Calendar.DAY_OF_MONTH) == day.getDay()) return true;

        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.GREEN));
        view.addSpan(new UnderlineSpan());
    }
}