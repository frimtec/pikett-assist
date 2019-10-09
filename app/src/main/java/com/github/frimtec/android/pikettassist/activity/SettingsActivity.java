package com.github.frimtec.android.pikettassist.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.PikettService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_SMS_CONFIRM_TEXT;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_TEST_ALARM_CHECK_TIME;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_TEST_ALARM_MESSAGE_PATTERN;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

  /**
   * A preference value change listener that updates the preference's summary
   * to reflect its new value.
   */
  private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
    String stringValue = value.toString();
    preference.setSummary(stringValue);
    return true;
  };

  private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToWeekdaysListener = (preference, value) -> {
    preference.setSummary(weekDaysValues(preference, (Set) value));
    return true;
  };

  /**
   * Helper method to determine if the device has an extra-large screen. For
   * example, 10" tablets are extra-large.
   */
  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  /**
   * Binds a preference's summary to its value. More specifically, when the
   * preference's value is changed, its summary (line of text below the
   * preference title) is updated to reflect the value. The summary is also
   * immediately updated upon calling this method. The exact display format is
   * dependent on the type of preference.
   *
   * @see #sBindPreferenceSummaryToValueListener
   */
  private static void bindPreferenceSummaryToValue(Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
        PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getString(preference.getKey(), ""));
  }

  private static void bindPreferenceSummaryToMultiValue(Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToWeekdaysListener);
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
        weekDaysValues(preference, PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getStringSet(preference.getKey(), Collections.emptySet())));
  }

  private static String weekDaysValues(Preference preference, Set<String> values) {
    String[] weekdays = preference.getContext().getResources().getStringArray(R.array.weekdays);
    return values.stream().map(id -> weekdays[Integer.parseInt(id) - 1]).collect(Collectors.joining(", "));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupActionBar();
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    startService(new Intent(this, PikettService.class));
  }

  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      if (!super.onMenuItemSelected(featureId, item)) {
        NavUtils.navigateUpFromSameTask(this);
      }
      return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onIsMultiPane() {
    return isXLargeTablet(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.pref_headers, target);
  }

  /**
   * This method stops fragment injection in malicious applications.
   * Make sure to deny any unknown fragments here.
   */
  protected boolean isValidFragment(String fragmentName) {
    return PreferenceFragment.class.getName().equals(fragmentName)
        || AlarmingFragment.CalendarFragment.class.getName().equals(fragmentName)
        || AlarmingFragment.class.getName().equals(fragmentName)
        || TestAlarmFragment.class.getName().equals(fragmentName)
        || NotificationPreferenceFragment.class.getName().equals(fragmentName);
  }

  /**
   * This fragment shows general preferences only. It is used when the
   * activity is showing a two-pane settings UI.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class AlarmingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_alarming);
      setHasOptionsMenu(true);
      bindPreferenceSummaryToValue(findPreference(PREF_KEY_SMS_CONFIRM_TEXT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
        return true;
      }
      return super.onOptionsItemSelected(item);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarFragment extends PreferenceFragment {

      @Override
      public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_calendar);
        setHasOptionsMenu(true);
        bindPreferenceSummaryToValue(findPreference(PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN));
      }
    }
  }

  /**
   * This fragment shows general preferences only. It is used when the
   * activity is showing a two-pane settings UI.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class TestAlarmFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_test_alarm);
      setHasOptionsMenu(true);
      bindPreferenceSummaryToValue(findPreference(PREF_KEY_TEST_ALARM_MESSAGE_PATTERN));
      bindPreferenceSummaryToValue(findPreference(PREF_KEY_TEST_ALARM_CHECK_TIME));
      bindPreferenceSummaryToValue(findPreference(PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES));
      bindPreferenceSummaryToMultiValue(findPreference(PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
        return true;
      }
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * This fragment shows notification preferences only. It is used when the
   * activity is showing a two-pane settings UI.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public static class NotificationPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_notification);
      setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
        return true;
      }
      return super.onOptionsItemSelected(item);
    }
  }

}
