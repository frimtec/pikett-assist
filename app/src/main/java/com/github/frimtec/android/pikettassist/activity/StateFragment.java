package com.github.frimtec.android.pikettassist.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.Feature;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.helper.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.AlarmService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static android.app.Activity.RESULT_OK;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.YELLOW;
import static com.github.frimtec.android.pikettassist.helper.Feature.RequestCodes.FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static com.github.frimtec.android.pikettassist.helper.Feature.SETTING_BATTERY_OPTIMIZATION_OFF;
import static com.github.frimtec.android.pikettassist.helper.Feature.SETTING_DRAW_OVERLAYS;
import static com.github.frimtec.android.pikettassist.helper.Feature.SMS_SERVICE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;

public class StateFragment extends AbstractListFragment<State> {

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";
  private static final String TAG = "StateFragment";

  static final int REQUEST_CODE_SELECT_PHONE_NUMBER = 111;

  private AlarmService alarmService;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alarmService = new AlarmService(this.getContext());
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      State selectedState = (State) listView.getItemAtPosition(position);
      selectedState.onClickAction(getContext());
    });
    registerForContextMenu(listView);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
      if (SETTING_DRAW_OVERLAYS.isAllowed(getContext())) {
        refresh();
        getContext().startService(new Intent(getContext(), PikettService.class));
      }
    } else if (requestCode == REQUEST_CODE_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
      Contact contact = ContactHelper.getContact(getContext(), data.getData());
      SharedState.setAlarmOperationsCenterContact(getContext(), contact);
      refresh();
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  protected ArrayAdapter<State> createAdapter() {
    List<State> states = new ArrayList<>();
    Optional<Feature> missingPermission = Arrays.stream(Feature.values())
        .filter(Feature::isPermissionType)
        .filter(set -> !set.isAllowed(getContext()))
        .findFirst();

    boolean missingPermissions = missingPermission.isPresent();
    if (missingPermissions) {
      Feature permission = missingPermission.get();
      if (permission.isSensitive()) {
        states.add(new State(R.drawable.ic_warning_black_24dp, getString(R.string.state_fragment_permissions), getString(permission.getNameResourceId()), null, RED) {
          @Override
          public void onClickAction(Context context) {
            permission.request(context, StateFragment.this);
          }
        });
      } else {
        permission.request(getContext(), this);
      }
    }

    boolean canDrawOverlays = SETTING_DRAW_OVERLAYS.isAllowed(getContext());
    if (!canDrawOverlays) {
      states.add(new State(R.drawable.ic_settings_black_24dp, getString(R.string.state_fragment_draw_overlays), getString(R.string.state_off), null, RED) {
        @Override
        public void onClickAction(Context context) {
          SETTING_DRAW_OVERLAYS.request(context, StateFragment.this);
        }
      });
    }

    if (!SETTING_BATTERY_OPTIMIZATION_OFF.isAllowed(getContext())) {
      states.add(new State(R.drawable.ic_battery_alert_black_24dp, getString(R.string.state_fragment_battery_optimization), getString(R.string.state_on), null, YELLOW) {
        @Override
        public void onClickAction(Context context) {
          SETTING_BATTERY_OPTIMIZATION_OFF.request(context, StateFragment.this);
        }
      });
    }

    if (!SMS_SERVICE.isAllowed(getContext())) {
      states.add(new State(R.drawable.ic_message_black_24dp, getString(R.string.state_fragment_sms_adapter), getString(R.string.state_fragment_sms_adapter_not_installed), null, RED) {
        @Override
        public void onClickAction(Context context) {
          SMS_SERVICE.request(context, StateFragment.this);
        }
      });
    }

    if (!missingPermissions && canDrawOverlays) {
      regularStates(states);
    }

    return new StateArrayAdapter(getContext(), states);
  }

  private void regularStates(List<State> states) {
    OnOffState pikettState = SharedState.getPikettState(getContext());
    Pair<AlarmState, Long> alarmState = SharedState.getAlarmState();
    State.TrafficLight alarmTrafficLight;
    String alarmValue;
    if (alarmState.first == AlarmState.ON) {
      alarmTrafficLight = RED;
      alarmValue = getString(R.string.alarm_state_on);
    } else if (alarmState.first == AlarmState.ON_CONFIRMED) {
      alarmTrafficLight = YELLOW;
      alarmValue = getString(R.string.alarm_state_on_confirmed);
    } else {
      alarmTrafficLight = pikettState == OnOffState.ON ? GREEN : OFF;
      alarmValue = getString(R.string.alarm_state_off);
    }

    boolean superviseSignalStrength = SharedState.getSuperviseSignalStrength(getContext());
    SignalStrengthHelper.SignalLevel level = SignalStrengthHelper.getSignalStrength(getContext());
    String signalStrength = level.toString(getContext());
    State.TrafficLight signalStrengthTrafficLight;
    if (!superviseSignalStrength) {
      signalStrengthTrafficLight = YELLOW;
    } else if (pikettState == OnOffState.OFF) {
      signalStrengthTrafficLight = OFF;
    } else if (level.ordinal() <= SignalStrengthHelper.SignalLevel.NONE.ordinal()) {
      signalStrengthTrafficLight = RED;
    } else if (level.ordinal() <= SignalStrengthHelper.SignalLevel.POOR.ordinal()) {
      signalStrengthTrafficLight = YELLOW;
    } else {
      signalStrengthTrafficLight = GREEN;
    }

    Supplier<Button> alarmCloseButtonSupplier = null;
    if (alarmState.first != AlarmState.OFF) {
      alarmCloseButtonSupplier = () -> {
        Button button = new Button(getContext());
        boolean unconfirmed = alarmState.first == AlarmState.ON;
        button.setText(unconfirmed ? getString(R.string.main_state_button_confirm_alert) : getString(R.string.main_state_button_close_alert));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
        button.setOnClickListener(v -> {
          if (unconfirmed) {
            alarmService.confirmAlarm();
          } else {
            try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
              ContentValues values = new ContentValues();
              values.put("end_time", Instant.now().toEpochMilli());
              int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " is null", null);
              if (update != 1) {
                Log.e(TAG, "One open case expected, but got " + update);
              }
            }
            NotificationHelper.cancelNotification(getContext(), NotificationHelper.ALERT_NOTIFICATION_ID);
          }
          refresh();
        });
        return button;
      };
    }

    boolean pikettStateManuallyOn = SharedState.getPikettStateManuallyOn(getContext());
    states.addAll(Arrays.asList(
        new OperationsCenterState(this, ContactHelper.getContact(getContext(), SharedState.getAlarmOperationsCenterContact(getContext()))),
        new State(
            R.drawable.ic_eye,
            getString(R.string.state_fragment_pikett_state),
            getString(pikettState == OnOffState.ON ? (pikettStateManuallyOn ? R.string.state_manually_on : R.string.state_on) : R.string.state_off),
            null,
            pikettState == OnOffState.ON ? (pikettStateManuallyOn ? YELLOW : GREEN) : OFF) {

          private static final int MENU_CONTEXT_SET_MANUALLY_ON = 1;
          private static final int MENU_CONTEXT_RESET = 2;

          @Override
          public void onCreateContextMenu(Context context, ContextMenu menu) {
            if (SharedState.getPikettStateManuallyOn(getContext())) {
              menu.add(Menu.NONE, MENU_CONTEXT_RESET, Menu.NONE, R.string.list_item_menu_reset);
            } else {
              menu.add(Menu.NONE, MENU_CONTEXT_SET_MANUALLY_ON, Menu.NONE, R.string.list_item_menu_set_manually_on);
            }
          }
          @Override
          public boolean onContextItemSelected(Context context, MenuItem item) {
            switch (item.getItemId()) {
              case MENU_CONTEXT_SET_MANUALLY_ON:
                SharedState.setPikettStateManuallyOn(context, true);
                context.startService(new Intent(context, SignalStrengthService.class));
                context.startService(new Intent(context, PikettService.class));
                StateFragment.this.refresh();
                return true;
              case MENU_CONTEXT_RESET:
                SharedState.setPikettStateManuallyOn(context, false);
                context.startService(new Intent(context, SignalStrengthService.class));
                context.startService(new Intent(context, PikettService.class));
                StateFragment.this.refresh();
                return true;
              default:
                return false;
            }
          }

          @Override
          public void onClickAction(Context context) {
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            startActivity(intent);
          }
        },
        new State(R.drawable.ic_siren, getString(R.string.state_fragment_alarm_state), alarmValue, alarmCloseButtonSupplier, alarmTrafficLight) {
          @Override
          public void onClickAction(Context context) {
            if (alarmState.second != null) {
              Intent intent = new Intent(getContext(), AlertDetailActivity.class);
              Bundle bundle = new Bundle();
              bundle.putLong(AlertDetailActivity.EXTRA_ALERT_ID, alarmState.second);
              intent.putExtras(bundle);
              startActivity(intent);
            } else {
              switchFragment(MainActivity.Fragment.CALL_LOG);
            }
          }
        },
        new State(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp, getString(R.string.state_fragment_signal_level),
            superviseSignalStrength ? (pikettState == OnOffState.ON ? signalStrength : getString(R.string.state_fragment_signal_level_supervise_enabled)) : getString(R.string.state_fragment_signal_level_supervise_disabled), null, signalStrengthTrafficLight) {

          private static final int MENU_CONTEXT_DEACTIVATE = 1;
          private static final int MENU_CONTEXT_ACTIVATE = 2;

          @Override
          public void onCreateContextMenu(Context context, ContextMenu menu) {
            if (SharedState.getSuperviseSignalStrength(context)) {
              menu.add(Menu.NONE, MENU_CONTEXT_DEACTIVATE, Menu.NONE, R.string.list_item_menu_deactivate);
            } else {
              menu.add(Menu.NONE, MENU_CONTEXT_ACTIVATE, Menu.NONE, R.string.list_item_menu_activate);
            }
          }

          @Override
          public boolean onContextItemSelected(Context context, MenuItem item) {
            switch (item.getItemId()) {
              case MENU_CONTEXT_DEACTIVATE:
                SharedState.setSuperviseSignalStrength(context, false);
                StateFragment.this.refresh();
                return true;
              case MENU_CONTEXT_ACTIVATE:
                SharedState.setSuperviseSignalStrength(context, true);
                StateFragment.this.refresh();
                return true;
              default:
                return false;
            }
          }
        })
    );
    String lastReceived = getString(R.string.state_fragment_test_alarm_never_received);
    for (String testContext : SharedState.getSuperviseTestContexts(getContext())) {
      OnOffState testAlarmState = OnOffState.OFF;
      Supplier<Button> testAlarmCloseButtonSupplier = null;
      try (SQLiteDatabase db = PAssist.getReadableDatabase()) {
        try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID, TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{testContext}, null, null, null)) {
          if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            lastReceived = formatDateTime(cursor.getLong(1) > 0 ? Instant.ofEpochMilli(cursor.getLong(1)) : null);
            testAlarmState = OnOffState.valueOf(cursor.getString(2));

            if (testAlarmState != OnOffState.OFF) {
              testAlarmCloseButtonSupplier = () -> {
                Button button = new Button(getContext());

                button.setText(getString(R.string.main_state_button_close_alert));
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
                button.setOnClickListener(v -> {
                  TestAlarmDao.updateAlarmState(testContext, OnOffState.OFF);
                  refresh();
                });
                return button;
              };
            }
          }
        }
        states.add(new State(R.drawable.ic_test_alarm, testContext, lastReceived, testAlarmCloseButtonSupplier, pikettState == OnOffState.ON ? (testAlarmState == OnOffState.ON ? RED : GREEN) : OFF) {
          @Override
          public void onClickAction(Context context) {
            Intent intent = new Intent(getContext(), TestAlarmDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, testContext);
            intent.putExtras(bundle);
            startActivity(intent);
          }
        });
      }
    }
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(StateFragment.DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    State selectedItem = (State) getListView().getItemAtPosition(info.position);
    selectedItem.onCreateContextMenu(getContext(), menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = getListView();
    State selectedItem = (State) listView.getItemAtPosition(info.position);
    boolean selected = selectedItem.onContextItemSelected(getContext(), item);
    if (selected) {
      return true;
    } else {
      return super.onContextItemSelected(item);
    }
  }

}
