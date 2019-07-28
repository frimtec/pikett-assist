package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallLogFragement extends Fragment {

  private static final String TAG = "CallLogFragement";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_list, container, false);
    ListView listView = view.findViewById(R.id.activity_list);

    List<Alert> alertList = new ArrayList<>();
    try (SQLiteDatabase db = PikettAssist.getReadableDatabase();
         Cursor cursor = db.rawQuery("SELECT _id, start_time, confirm_time, end_time FROM t_alert ORDER BY start_time DESC", null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          alertList.add(new Alert(
              id,
              Instant.ofEpochMilli(cursor.getLong(1)),
              cursor.getLong(2) > 0 ? Instant.ofEpochMilli(cursor.getLong(2)) : null,
              cursor.getLong(3) > 0 ? Instant.ofEpochMilli(cursor.getLong(3)) : null,
              Collections.emptyList()));
        } while (cursor.moveToNext());
      }
    }
    ArrayAdapter<Alert> adapter = new AlertArrayAdapter(getContext(), alertList);
    listView.setAdapter(adapter);
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Alert selectedAlert = (Alert) listView.getItemAtPosition(position);
      long eventId = selectedAlert.getId();
      Log.v(TAG, "Selected alert: " + selectedAlert.getId());
    });
    return view;
  }

}
