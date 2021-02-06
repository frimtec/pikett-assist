package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Optional;

public class ShiftService {

  private final Context context;
  private final ShiftDao shiftDao;

  public ShiftService(Context context) {
    this.context = context;
    this.shiftDao = new ShiftDao(context);
  }

  public OnOffState getState() {
    return ApplicationState.instance().getPikettStateManuallyOn() ||
        hasShiftEventForNow(
            ApplicationPreferences.instance().getCalendarEventPikettTitlePattern(this.context),
            ApplicationPreferences.instance().getCalendarSelection(this.context),
            ApplicationPreferences.instance().getPrePostRunTime(context)
        ) ? OnOffState.ON : OnOffState.OFF;
  }

  public Optional<Shift> findCurrentOrNextShift(Instant now) {
    ApplicationPreferences preferences = ApplicationPreferences.instance();
    return this.shiftDao.getShifts(
        preferences.getCalendarEventPikettTitlePattern(this.context),
        preferences.getCalendarSelection(this.context),
        preferences.getPartnerExtractionEnabled(this.context) ? preferences.getPartnerSearchExtractPattern(this.context) : ""
    ).stream()
        .filter(shift -> !shift.isOver(now, preferences.getPrePostRunTime(context)))
        .findFirst();
  }

  private boolean hasShiftEventForNow(String eventTitleFilterPattern, String calendarSelection, Duration prePostRunTime) {
    return this.shiftDao.getShifts(eventTitleFilterPattern, calendarSelection, null).stream()
        .anyMatch(shift -> shift.isNow(prePostRunTime));
  }


}
