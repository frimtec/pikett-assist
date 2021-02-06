package com.github.frimtec.android.pikettassist.ui.shifts;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;
import static com.github.frimtec.android.pikettassist.ui.FragmentName.SHIFTS;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.translatedFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;

public class ShiftListFragment extends AbstractListFragment<Shift> {

  private View headerView;

  public ShiftListFragment() {
    super(SHIFTS);
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Shift selectedShift = (Shift) listView.getItemAtPosition(position);
      if (selectedShift != null) {
        long eventId = selectedShift.getId();
        Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, selectedShift.getStartTime().toEpochMilli());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, selectedShift.getEndTime().toEpochMilli());
        startActivity(intent);
      }
    });
    this.headerView = getLayoutInflater().inflate(R.layout.shift_header, listView, false);
    listView.addHeaderView(this.headerView);
  }

  @Override
  protected ArrayAdapter<Shift> createAdapter() {
    List<Shift> shifts;
    Instant now = Shift.now();
    Context context = requireContext();
    Duration prePostRunTime = ApplicationPreferences.instance().getPrePostRunTime(context);
    if (!PERMISSION_CALENDAR_READ.isAllowed(context)) {
      Toast.makeText(context, getString(R.string.missing_permission_calendar_access), Toast.LENGTH_LONG).show();
      shifts = Collections.emptyList();
    } else {
      shifts = new ShiftDao(context).getShifts(ApplicationPreferences.instance().getCalendarEventPikettTitlePattern(context), ApplicationPreferences.instance().getCalendarSelection(context), null)
          .stream().filter(shift -> !shift.isOver(now, prePostRunTime)).collect(Collectors.toList());
      if (shifts.isEmpty()) {
        Toast.makeText(context, getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
      }
    }
    updateHeader(now, shifts);
    return new ShiftArrayAdapter(context, shifts);
  }

  private void updateHeader(Instant now, List<Shift> shifts) {
    TextView nextLabel = headerView.findViewById(R.id.shift_header_next_label);
    TextView nextValue = headerView.findViewById(R.id.shift_header_next_value);
    String label;
    String value;
    if (shifts.isEmpty()) {
      label = getString(R.string.shift_header_next_label_no_entry);
      value = "";
    } else {
      Shift shift = shifts.get(0);
      Duration duration;
      if (now.isBefore(shift.getStartTime())) {
        label = getString(R.string.shift_header_next_label_starts);
        duration = Duration.between(now, shift.getStartTime());
      } else {
        label = getString(R.string.shift_header_next_label_ends);
        duration = Duration.between(now, shift.getEndTime());
      }
      value = toDurationString(duration, translatedFormatter(getContext()));
    }
    nextLabel.setText(label);
    nextValue.setText(value);
  }

}
