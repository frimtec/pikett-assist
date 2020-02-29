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

import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.getCalendarEventPikettTitlePattern;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.getCalendarSelection;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.getPrePostRunTime;

public class ShiftService {

  private final Context context;
  private final ShiftDao shiftDao;

  public ShiftService(Context context) {
    this.context = context;
    this.shiftDao = new ShiftDao(context);
  }

  public OnOffState getState() {
    return ApplicationState.getPikettStateManuallyOn() ||
        hasShiftEventForNow(
            getCalendarEventPikettTitlePattern(this.context),
            getCalendarSelection(this.context),
            getPrePostRunTime(context)
        ) ? OnOffState.ON : OnOffState.OFF;
  }

  public Optional<Shift> findCurrentOrNextShift(Instant now) {
    return this.shiftDao.getShifts(ApplicationPreferences.getCalendarEventPikettTitlePattern(this.context), getCalendarSelection(this.context))
        .stream().filter(shift -> !shift.isOver(now, getPrePostRunTime(context))).findFirst();
  }

  private boolean hasShiftEventForNow(String eventTitleFilterPattern, String calendarSelection, Duration prePostRunTime) {
    return this.shiftDao.getShifts(eventTitleFilterPattern, calendarSelection).stream()
        .anyMatch(shift -> shift.isNow(prePostRunTime));
  }


}
