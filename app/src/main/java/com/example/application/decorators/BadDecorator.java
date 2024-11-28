package com.example.application.decorators;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.application.R;
import com.example.application.structs.DiaryEntry;
import com.example.application.structs.Mood;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

public class BadDecorator implements DayViewDecorator {
    private final Drawable badDrawable;
    private final List<DiaryEntry> data;

    @SuppressLint("UseCompatLoadingForDrawables")
    public BadDecorator(Context context, List<DiaryEntry> data) {
        badDrawable = context.getResources().getDrawable(R.drawable.bad);
        this.data = data;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (data == null) {
            return false;
        } else {
            for (DiaryEntry de : data) {
                if (de.getMood().equals(Mood.BAD)) {
                    if (day.getDay() == de.getFormattedDate().getDate()) return true;
                }
            }

            return false;
        }
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(badDrawable);
    }
}