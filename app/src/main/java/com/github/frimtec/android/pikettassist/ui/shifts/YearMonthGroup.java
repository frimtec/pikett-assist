package com.github.frimtec.android.pikettassist.ui.shifts;

import com.github.frimtec.android.pikettassist.domain.Shift;

import java.time.YearMonth;
import java.util.List;

public record YearMonthGroup(YearMonth yearMonth, List<Shift> shifts) {


}
