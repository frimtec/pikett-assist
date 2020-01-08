package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.SharedState;

import static com.github.frimtec.android.pikettassist.state.SharedState.getCalendarEventPikettTitlePattern;
import static com.github.frimtec.android.pikettassist.state.SharedState.getPikettStateManuallyOn;

public class ShiftService {

  private final Context context;
  private final ShiftDao shiftDao;

  public ShiftService(Context context) {
    this.context = context;
    this.shiftDao = new ShiftDao(context);
  }

  public OnOffState getState() {
    return getPikettStateManuallyOn(this.context) ||
        hasShiftEventForNow(getCalendarEventPikettTitlePattern(this.context), SharedState.getCalendarSelection(this.context)) ? OnOffState.ON : OnOffState.OFF;
  }

  private boolean hasShiftEventForNow(String eventTitleFilterPattern, String calendarSelection) {
    return this.shiftDao.getShifts(eventTitleFilterPattern, calendarSelection).stream()
        .anyMatch(Shift::isNow);
  }


}
