package com.github.frimtec.android.pikettassist.activity;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
    Instant now = PikettShift.now();
    List<PikettShift> shifts = CalendarEventHelper.getPikettShifts(getContext(), SharedState.getCalendarEventPikettTitlePattern(getContext()), SharedState.getCalendarSelection(getContext()))
        .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
    return new PikettShiftArrayAdapter(getContext(), shifts);
  }

}
