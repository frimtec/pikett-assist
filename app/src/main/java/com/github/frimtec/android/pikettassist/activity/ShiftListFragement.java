package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
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
    List<PikettShift> shifts = CalendarEventHelper.getPikettShifts(getContext(), SharedState.getCalendarEventPikettTitlePattern(getContext()))
        .stream().filter(shift -> !shift.isOver(now)).collect(Collectors.toList());
    ArrayAdapter<PikettShift> adapter = new PikettShiftArrayAdapter(getContext(), shifts);
    listView.setAdapter(adapter);
    return view;
  }

}
