package com.github.frimtec.android.pikettassist.activity;

import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.android.billingclient.api.Purchase;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingConstants;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.Feature;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper.SignalLevel;
import com.github.frimtec.android.pikettassist.helper.TestAlarmDao;
import com.github.frimtec.android.pikettassist.receiver.SmsListener;
import com.github.frimtec.android.pikettassist.service.AlarmService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.YELLOW;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_LOADED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_PURCHASED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PENDING;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.helper.Feature.RequestCodes.FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static com.github.frimtec.android.pikettassist.helper.Feature.SETTING_BATTERY_OPTIMIZATION_OFF;
import static com.github.frimtec.android.pikettassist.helper.Feature.SETTING_DRAW_OVERLAYS;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;

public class StateFragment extends AbstractListFragment<State> implements BillingManager.BillingUpdatesListener {

  public static final String DIALOG_TAG = "dialog";

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";
  private static final String TAG = "StateFragment";

  static final int REQUEST_CODE_SELECT_PHONE_NUMBER = 111;
  public static final String SECURE_SMS_PROXY_PACKAGE_NAME = "com.github.frimtec.android.securesmsproxy";

  private final Random random = new Random(System.currentTimeMillis());

  private AlarmService alarmService;
  private SecureSmsProxyFacade s2smp;
  private MainActivity parent;

  private BillingState bronzeSponsor = NOT_LOADED;
  private BillingState silverSponsor = NOT_LOADED;
  private BillingState goldSponsor = NOT_LOADED;

  private SignalStrengthHelper signalStrengthHelper;

  public void setParent(MainActivity parent) {
    this.parent = parent;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alarmService = new AlarmService(this.getContext());
    this.s2smp = SecureSmsProxyFacade.instance(this.getContext());
    this.signalStrengthHelper = new SignalStrengthHelper(this.getContext());
  }

  @Override
  public void onResume() {
    // the configured subscription may have been changed
    this.signalStrengthHelper = new SignalStrengthHelper(this.getContext());
    super.onResume();
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
        getContext().startService(new Intent(getContext(), PikettService.class));
      }
    } else if (requestCode == REQUEST_CODE_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
      Contact contact = ContactHelper.getContact(getContext(), data.getData());
      SharedState.setAlarmOperationsCenterContact(getContext(), contact);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  protected ArrayAdapter<State> createAdapter() {
    List<State> states = new LinkedList<>();
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

    if (!missingPermissions && canDrawOverlays) {
      regularStates(states);
    }

    return new StateArrayAdapter(getContext(), new ArrayList<>(states));
  }

  private void regularStates(List<State> states) {
    Installation installation = this.s2smp.getInstallation();
    boolean installed = installation.getAppVersion().isPresent();
    Set<String> phoneNumbers = ContactHelper.getPhoneNumbers(getContext(), SharedState.getAlarmOperationsCenterContact(getContext()));
    boolean allowed = phoneNumbers.isEmpty() || s2smp.isAllowed(phoneNumbers);
    boolean newVersion = installed && installation.getApiVersion().compareTo(installation.getAppVersion().get()) > 0;
    PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);

    boolean batteryOptimisationOn = !pm.isIgnoringBatteryOptimizations(SECURE_SMS_PROXY_PACKAGE_NAME);

    PackageManager packageManager = getContext().getPackageManager();
    boolean smsAdapterSmsPermission = packageManager.checkPermission(RECEIVE_SMS, SECURE_SMS_PROXY_PACKAGE_NAME) == PERMISSION_GRANTED &&
        packageManager.checkPermission(SEND_SMS, SECURE_SMS_PROXY_PACKAGE_NAME) == PERMISSION_GRANTED;

    states.add(new State(R.drawable.ic_message_black_24dp, getString(R.string.state_fragment_sms_adapter),
        getSmsAdapterValue(installation, installed, allowed, newVersion, batteryOptimisationOn, smsAdapterSmsPermission), null,
        getSmsAdapterState(installed, allowed, newVersion, batteryOptimisationOn, smsAdapterSmsPermission)) {
      @Override
      public void onClickAction(Context context) {
        if (!installed) {
          SpannableString message = new SpannableString(Html.fromHtml(context.getString(R.string.permission_sms_text), Html.FROM_HTML_MODE_COMPACT));
          openDownloadDialog(context, message, R.string.permission_sms_title, installation);
          return;
        }
        if (smsAdapterSmsPermission) {
          if (!allowed) {
            Set<String> phoneNumbers = ContactHelper.getPhoneNumbers(getContext(), SharedState.getAlarmOperationsCenterContact(getContext()));
            StateFragment.this.s2smp.register(StateFragment.this.parent, 1000, phoneNumbers, SmsListener.class);
            return;
          }
          if (newVersion) {
            SpannableString message = new SpannableString(Html.fromHtml(context.getString(R.string.permission_sms_update_text), Html.FROM_HTML_MODE_COMPACT));
            openDownloadDialog(context, message, R.string.permission_sms_update_title, installation);
            return;
          }
          if (batteryOptimisationOn) {
            NotificationHelper.infoDialog(context, R.string.notification_battery_optimization_title, R.string.notification_battery_optimization_s2smp_text,
                (dialogInterface, integer) -> startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));
            return;
          }
        }
        Intent launchIntent = packageManager.getLaunchIntentForPackage(SecureSmsProxyFacade.S2SMP_PACKAGE_NAME);
        if (launchIntent != null) {
          startActivity(launchIntent);
        }
      }

      private void openDownloadDialog(Context context, SpannableString message, @StringRes int title, Installation installation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
          // let the browser handle the stuff
          AlertDialog alertDialog = new AlertDialog.Builder(context)
              // set dialog message
              .setTitle(title)
              .setMessage(message)
              .setCancelable(true)
              .setPositiveButton(R.string.general_download, (dialog, which) -> {
                Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, installation.getDownloadLink());
                openBrowserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openBrowserIntent);
              })
              .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
              }).create();
          alertDialog.show();
          ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else {
          if (!context.getPackageManager().canRequestPackageInstalls()) {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                // set dialog message
                .setTitle(title)
                .setMessage(message + "\n\n" + context.getString(R.string.permission_sms_text_unknown_source_request))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.s2smp_settings), (dialog, which) -> {
                  startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:com.github.frimtec.android.pikettassist")));
                })
                .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
                }).create();
            alertDialog.show();
          } else {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                // set dialog message
                .setTitle(title)
                .setMessage(getString(R.string.s2smp_download_request))
                .setCancelable(true)
                .setPositiveButton(R.string.general_download, (dialog, which) -> {
                  DownloadManager.Request request = new DownloadManager.Request(installation.getDownloadLink());
                  request.setTitle("S2SMP version " + installation.getApiVersion());
                  request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                  request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.format("s2smp-app-%s.apk", installation.getApiVersion()));
                  DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                  if (manager != null) {
                    manager.enqueue(request);
                    Toast.makeText(context, R.string.s2smp_download_started, Toast.LENGTH_LONG).show();
                  }
                })
                .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
                }).create();
            alertDialog.show();
          }
        }
      }

      private static final int MENU_CONTEXT_VIEW = 1;
      private static final int SEND_TEST_SMS = 2;


      @Override
      public void onCreateContextMenu(Context context, ContextMenu menu) {
        super.onCreateContextMenu(context, menu);
        boolean present = StateFragment.this.s2smp.getInstallation().getAppVersion().isPresent();
        menu.add(Menu.NONE, MENU_CONTEXT_VIEW, Menu.NONE, R.string.list_item_menu_view)
            .setEnabled(present);
        menu.add(Menu.NONE, SEND_TEST_SMS, Menu.NONE, R.string.list_item_menu_send_test_sms)
            .setEnabled(present && !TextUtils.isEmpty(SharedState.getSmsAdapterSecret(context)));
      }

      @Override
      public boolean onContextItemSelected(Context context, MenuItem item) {
        switch (item.getItemId()) {
          case MENU_CONTEXT_VIEW:
            Intent launchIntent = packageManager.getLaunchIntentForPackage(SecureSmsProxyFacade.S2SMP_PACKAGE_NAME);
            if (launchIntent != null) {
              startActivity(launchIntent);
            }
            return true;
          case SEND_TEST_SMS:
            StateFragment.this.s2smp.sendSms(new Sms(SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK, ":-)"), SharedState.getSmsAdapterSecret(context));
            Toast.makeText(context, R.string.state_fragment_loopback_sms_sent, Toast.LENGTH_SHORT).show();
            return true;
          default:
            return false;
        }
      }
    });

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
    SignalLevel level = this.signalStrengthHelper.getSignalStrength();
    String signalStrength = level.toString(getContext());
    String networkOperatorName = this.signalStrengthHelper.getNetworkOperatorName();
    State.TrafficLight signalStrengthTrafficLight;
    if (!superviseSignalStrength) {
      signalStrengthTrafficLight = YELLOW;
    } else if (pikettState == OnOffState.OFF) {
      signalStrengthTrafficLight = OFF;
    } else if (level.ordinal() <= SignalLevel.NONE.ordinal()) {
      signalStrengthTrafficLight = RED;
    } else if (level.ordinal() <= SharedState.getSuperviseSignalStrengthMinLevel(getContext())) {
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
          private static final int MENU_CONTEXT_CREATE_ALARM_MANUALLY = 1;

          @Override
          public void onClickAction(Context context) {
            if (alarmState.second != null) {
              Intent intent = new Intent(getContext(), AlertDetailActivity.class);
              Bundle bundle = new Bundle();
              bundle.putLong(AlertDetailActivity.EXTRA_ALERT_ID, alarmState.second);
              intent.putExtras(bundle);
              startActivity(intent);
            }
          }

          @Override
          public void onCreateContextMenu(Context context, ContextMenu menu) {
            if (pikettState == OnOffState.ON) {
              menu.add(Menu.NONE, MENU_CONTEXT_CREATE_ALARM_MANUALLY, Menu.NONE, R.string.menu_create_manually_alarm);
            }
          }

          @Override
          public boolean onContextItemSelected(Context context, MenuItem item) {
            switch (item.getItemId()) {
              case MENU_CONTEXT_CREATE_ALARM_MANUALLY:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.manually_created_alarm_reason));
                EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(R.string.manually_created_alarm_reason_default);
                input.requestFocus();
                builder.setView(input);
                builder.setPositiveButton(R.string.general_ok, (dialog, which) -> {
                  dialog.dismiss();
                  String comment = input.getText().toString();
                  AlarmService alarmService = new AlarmService(getContext());
                  alarmService.newManuallyAlarm(Instant.now(), comment);
                  refresh();
                });
                builder.setNegativeButton(R.string.general_cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
              default:
                return false;
            }
          }

        },
        new State(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp, networkOperatorName != null ? String.format("%s %s", getString(R.string.state_fragment_signal_level), networkOperatorName) : getString(R.string.state_fragment_signal_level),
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

    if (SharedState.getTestAlarmEnabled(getContext())) {
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

    if (Stream.of(this.bronzeSponsor, silverSponsor, goldSponsor).allMatch(billing -> billing != NOT_LOADED) &&
        Stream.of(this.bronzeSponsor, silverSponsor, goldSponsor).noneMatch(billing -> billing == PURCHASED) &&
        randomizedOn()) {
      states.add(this.random.nextInt(states.size() + 1), new State(R.drawable.ic_monetization_on_black_24dp, getString(R.string.state_fragment_donation), getString(R.string.state_fragment_donation_value), null, YELLOW) {
        @Override
        public void onClickAction(Context context) {
          parent.showDonationDialog();
        }
      });
    }
  }

  private State.TrafficLight getSmsAdapterState(boolean installed, boolean allowed, boolean newVersion, boolean batteryOptimisationOn, boolean smsAdapterSmsPermission) {
    if (!installed || !smsAdapterSmsPermission || !allowed) {
      return RED;
    } else if (newVersion || batteryOptimisationOn) {
      return YELLOW;
    } else {
      return GREEN;
    }
  }

  private String getSmsAdapterValue(Installation installation, boolean installed, boolean allowed, boolean newVersion, boolean batteryOptimisationOn, boolean smsAdapterSmsPermission) {
    if(!installed) {
      return getString(R.string.state_fragment_sms_adapter_not_installed);
    } else if(!smsAdapterSmsPermission) {
      return getString(R.string.state_fragment_sms_adapter_no_sms_permissions);
    } else if(!allowed) {
      return getString(R.string.state_fragment_phone_numbers_blocked);
    } else if(newVersion) {
      return getString(R.string.state_fragment_s2smp_requires_update);
    } else if(batteryOptimisationOn) {
      return getString(R.string.notification_battery_optimization_short_title);
    } else {
      return "S2SMP V" + installation.getAppVersion().get();
    }
  }

  private boolean randomizedOn() {
    long installationAgeInDays = Integer.MAX_VALUE;
    try {
      PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
      installationAgeInDays = Duration.between(Instant.ofEpochMilli(packageInfo.firstInstallTime), Instant.now()).toDays();
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Can not get package info", e);
    }
    return this.random.nextFloat() <= Math.min((installationAgeInDays - 30f) * 0.01f, 0.3f);
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

  @Override
  public void onBillingClientSetupFinished() {
    DonationFragment donationFragment = (DonationFragment) parent.getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
    if (donationFragment != null) {
      donationFragment.onManagerReady(parent);
    }
  }

  @Override
  public void onPurchasesUpdated(List<Purchase> purchases) {
    bronzeSponsor = NOT_PURCHASED;
    silverSponsor = NOT_PURCHASED;
    goldSponsor = NOT_PURCHASED;
    for (Purchase purchase : purchases) {
      BillingState state = getBillingState(purchase);
      switch (purchase.getSku()) {
        case BillingConstants.SKU_SPONSOR_BRONZE:
          bronzeSponsor = state;
          break;
        case BillingConstants.SKU_SPONSOR_SILVER:
          silverSponsor = state;
          break;
        case BillingConstants.SKU_SPONSOR_GOLD:
          goldSponsor = state;
          break;
        default:
          Log.e(TAG, "Has unknown product: " + purchase.getSku());
      }
    }
    DonationFragment donationFragment = (DonationFragment) parent.getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
    if (donationFragment != null) {
      donationFragment.refreshUI();
    }
  }

  private BillingState getBillingState(Purchase purchase) {
    switch (purchase.getPurchaseState()) {
      case Purchase.PurchaseState.PURCHASED:
        return PURCHASED;
      case Purchase.PurchaseState.PENDING:
        return PENDING;
      case Purchase.PurchaseState.UNSPECIFIED_STATE:
      default:
        return NOT_PURCHASED;
    }
  }

  public BillingState getBronzeSponsor() {
    return bronzeSponsor;
  }

  public BillingState getSilverSponsor() {
    return silverSponsor;
  }

  public BillingState getGoldSponsor() {
    return goldSponsor;
  }

}
