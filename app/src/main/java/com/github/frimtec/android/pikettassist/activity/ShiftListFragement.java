package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ShiftListFragement extends Fragment {

  private static final String TAG = "ListFragement";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_list, container, false);
    ListView listView = view.findViewById(R.id.activity_list);
    Instant now = PikettShift.now();
    List<PikettShift> shifts = CalendarEventHelper.getPikettShifts(getContext(), SharedState.getCalendarEventPikettTitlePattern(getContext()), SharedState.getCalendarSelection(getContext()))
        .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
    ArrayAdapter<PikettShift> adapter = new PikettShiftArrayAdapter(getContext(), shifts);
    listView.setAdapter(adapter);
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      PikettShift selectedShift = (PikettShift) listView.getItemAtPosition(position);
      long eventId = selectedShift.getId();
      Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
      Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
      startActivity(intent);
    });
    return view;
  }

  public void refresh() {
    ListView listView = view.findViewById(R.id.activity_list);
    Instant now = PikettShift.now();
    List<PikettShift> shifts = CalendarEventHelper.getPikettShifts(getContext(), SharedState.getCalendarEventPikettTitlePattern(getContext()), SharedState.getCalendarSelection(getContext()))
        .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
    ArrayAdapter<PikettShift> adapter = new PikettShiftArrayAdapter(getContext(), shifts);
    listView.setAdapter(adapter);
  }
}
