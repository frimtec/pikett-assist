package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.domain.OnOffState.OFF;
import static com.github.frimtec.android.pikettassist.domain.OnOffState.ON;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.domain.ShiftState;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class ShiftService {

  private final Context context;
  private final ShiftDao shiftDao;

  public ShiftService(Context context) {
    this.context = context;
    this.shiftDao = new ShiftDao(context);
  }

  public ShiftState getShiftState() {
    if (ApplicationState.instance().getPikettStateManuallyOn()) {
      return new ShiftState(ON);
    }
    return hasShiftEventForNow(
        ApplicationPreferences.instance().getCalendarEventPikettTitlePattern(this.context),
        ApplicationPreferences.instance().getCalendarSelection(this.context),
        ApplicationPreferences.instance().getPrePostRunTime(context)
    ).map(ShiftState::new).orElse(new ShiftState(OFF));
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

  private Optional<Shift> hasShiftEventForNow(String eventTitleFilterPattern, String calendarSelection, Duration prePostRunTime) {
    return this.shiftDao.getShifts(eventTitleFilterPattern, calendarSelection, null).stream()
        .filter(shift -> shift.isNow(prePostRunTime))
        .findFirst();
  }


}
