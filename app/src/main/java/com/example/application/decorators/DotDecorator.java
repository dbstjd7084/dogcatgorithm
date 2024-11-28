package com.example.application.decorators;

import com.example.application.structs.DiaryEntry;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.List;

public class DotDecorator implements DayViewDecorator {
    private final int color;
    private final List<DiaryEntry> dates;

    public DotDecorator(int color, List<DiaryEntry> dates) {
        this.color = color;
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (dates == null) {
            return false;
        } else {
            for (DiaryEntry de : dates) {
                if (de.getTitle() != null ||
                de.getComment() != null ||
                de.getImageUriList().size() > 0) {
                    if (day.getDay() == de.getFormattedDate().getDate()) return true;
                }
            }
            return false;
        }
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(4, color));
    }
}
