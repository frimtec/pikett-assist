package com.github.frimtec.android.pikettassist.ui.shifts;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.SharedState;

import org.threeten.bp.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.ui.FragmentName.SHIFTS;
import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;

public class ShiftListFragment extends AbstractListFragment<Shift> {

  public ShiftListFragment() {
    super(SHIFTS);
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Shift selectedShift = (Shift) listView.getItemAtPosition(position);
      long eventId = selectedShift.getId();
      Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
      Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
      startActivity(intent);
    });
  }

  @Override
  protected ArrayAdapter<Shift> createAdapter() {
    List<Shift> shifts;
    if (!PERMISSION_CALENDAR_READ.isAllowed(getContext())) {
      Toast.makeText(getContext(), getContext().getString(R.string.missing_permission_calendar_access), Toast.LENGTH_LONG).show();
      shifts = Collections.emptyList();
    } else {
      Instant now = Shift.now();
      shifts = new ShiftDao(getContext()).getShifts(SharedState.getCalendarEventPikettTitlePattern(getContext()), SharedState.getCalendarSelection(getContext()))
          .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
      if (shifts.isEmpty()) {
        Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
      }
    }
    return new ShiftArrayAdapter(getContext(), shifts);
  }

}
