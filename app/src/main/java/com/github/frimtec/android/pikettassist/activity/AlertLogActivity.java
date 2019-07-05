package com.github.frimtec.android.pikettassist.activity;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class AlertLogActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_alert_log);
    displayList();
  }

  public void displayList() {
    try(SQLiteDatabase db = PikettAssist.getReadableDatabase()) {
      Cursor cursor = db.rawQuery("SELECT _id, start_time, end_time FROM t_alert ORDER BY start_time DESC", null);
      String[] from = new String[]{"start_time", "end_time"};
      int[] to = new int[]{R.id.textView};
      SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_item, cursor, from, to, 0);
      adapter.setViewBinder((view, cursor1, columnIndex) -> {
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(cursor1.getLong(1)), ZoneId.systemDefault());
        long endTimeAsLong = cursor1.getLong(2);
        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeAsLong), ZoneId.systemDefault());
        TextView textView = (TextView) view;
        textView.setText(Html.fromHtml("<table><tr><td>" +
            startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss", Locale.getDefault())) + " - " +
            (endTimeAsLong > 0 ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss", Locale.getDefault())) : "") +  "</td>" +
            "</tr></table>", Html.FROM_HTML_MODE_COMPACT));
        return true;
      });
      ListView listView = getListView();
      listView.setAdapter(adapter);
    }
  }

}
