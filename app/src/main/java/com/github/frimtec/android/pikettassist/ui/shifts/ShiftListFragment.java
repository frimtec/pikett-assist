package com.github.frimtec.android.pikettassist.ui.shifts;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.translatedFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.dao.ShiftDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter.Group;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;

import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShiftListFragment extends AbstractListFragment {

  private View headerView;

  public ShiftListFragment() {
    super(FragmentPosition.SHIFTS);
  }

  @Override
  protected void configureListView(ExpandableListView listView) {
    listView.setClickable(true);
    listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
      Group<YearMonth, Shift> selectedGroup = (Group<YearMonth, Shift>) listView.getExpandableListAdapter().getGroup(groupPosition);
      Shift selectedShift = selectedGroup.items().get(childPosition);
      if (selectedShift != null) {
        long eventId = selectedShift.getId();
        Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, selectedShift.getStartTime().toEpochMilli());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, selectedShift.getEndTime().toEpochMilli());
        startActivity(intent);
        return true;
      }
      return false;
    });

    this.headerView = getLayoutInflater().inflate(R.layout.shift_header, listView, false);
    listView.addHeaderView(this.headerView);
    listView.setOnGroupExpandListener(groupPosition -> changeExpandedGroupsPreferences(listView, expandedGroup -> {
      expandedGroup.add(((Group<YearMonth, Shift>) listView.getExpandableListAdapter().getGroup(groupPosition)).key());
      return expandedGroup;
    }));
    listView.setOnGroupCollapseListener(groupPosition -> changeExpandedGroupsPreferences(listView, expandedGroup -> {
      expandedGroup.remove(((Group<YearMonth, Shift>) listView.getExpandableListAdapter().getGroup(groupPosition)).key());
      return expandedGroup;
    }));
  }

  private void changeExpandedGroupsPreferences(ExpandableListView listView, Function<Set<YearMonth>, Set<YearMonth>> transformer) {
    ExpandableListAdapter adapter = listView.getExpandableListAdapter();
    Set<YearMonth> yearMonths = new HashSet<>();
    IntStream.range(0, adapter.getGroupCount()).forEach(i -> {
      Group<YearMonth, Shift> item = (Group<YearMonth, Shift>) adapter.getGroup(i);
      yearMonths.add(item.key());
    });
    ApplicationPreferences applicationPreferences = ApplicationPreferences.instance();
    Set<YearMonth> expandedGroups = applicationPreferences.getExpandedShiftGroups(getContext());
    expandedGroups.retainAll(yearMonths);
    applicationPreferences.setExpandedShiftGroups(getContext(), transformer.apply(expandedGroups));
  }

  @Override
  protected Set<Integer> getExpandedGroups(ExpandableListView listView) {
    ExpandableListAdapter adapter = listView.getExpandableListAdapter();
    Map<YearMonth, Integer> yearToPosition = IntStream.range(0, adapter.getGroupCount())
        .boxed()
        .collect(Collectors.toMap(
            i -> ((Group<YearMonth, Shift>) adapter.getGroup(i)).key(),
            i -> i
        ));
    HashSet<Integer> expandedGroups = ApplicationPreferences.instance().getExpandedShiftGroups(getContext()).stream()
        .filter(yearToPosition::containsKey)
        .map(yearToPosition::get)
        .collect(Collectors.toCollection(HashSet::new));
    if (adapter.getGroupCount() > 0) {
      expandedGroups.add(0);
    }
    return expandedGroups;
  }

  @Override
  protected ExpandableListAdapter createAdapter() {
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
    return new ShiftExpandableListAdapter(context, shifts);
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
