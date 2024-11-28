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

public class HappyDecorator implements DayViewDecorator {
    private final Drawable happyDrawable;
    private final List<DiaryEntry> data;

    @SuppressLint("UseCompatLoadingForDrawables")
    public HappyDecorator(Context context, List<DiaryEntry> data) {
        happyDrawable = context.getResources().getDrawable(R.drawable.happy);
        this.data = data;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (data == null) {
            return false;
        } else {
            for (DiaryEntry de : data) {
                if (de.getMood().equals(Mood.GOOD)) {
                    if (day.getDay() == de.getFormattedDate().getDate()) return true;
                }
            }

            return false;
        }
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(happyDrawable);
    }
}