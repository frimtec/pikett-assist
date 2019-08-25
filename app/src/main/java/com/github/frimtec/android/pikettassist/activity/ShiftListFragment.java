package com.github.frimtec.android.pikettassist.activity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ShiftListFragment extends AbstractListFragment<PikettShift> {

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      PikettShift selectedShift = (PikettShift) listView.getItemAtPosition(position);
      long eventId = selectedShift.getId();
      Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
      Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
      startActivity(intent);
    });
  }

  @Override
  protected ArrayAdapter<PikettShift> createAdapter() {
    List<PikettShift> shifts;
    if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PERMISSION_GRANTED) {
      Toast.makeText(getContext(), getContext().getString(R.string.missing_permission_calendar_access), Toast.LENGTH_LONG).show();
      shifts = Collections.emptyList();
    } else {
      Instant now = PikettShift.now();
      shifts = CalendarEventHelper.getPikettShifts(getContext(), SharedState.getCalendarEventPikettTitlePattern(getContext()), SharedState.getCalendarSelection(getContext()))
          .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
      if (shifts.isEmpty()) {
        Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
      }
    }
    return new PikettShiftArrayAdapter(getContext(), shifts);
  }

}
