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
import android.widget.Button;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;

public class StateFragement extends Fragment {

    private static final String TAG = "StateFragement";

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_state, container, false);
        Button button = (Button) view.findViewById(R.id.close_alert_button);
        button.setOnClickListener(v -> {
            try (SQLiteDatabase writableDatabase = PikettAssist.getWritableDatabase()) {
                Log.v(TAG, "Close alert button pressed.");
                ContentValues values = new ContentValues();
                values.put("end_time", Instant.now().toEpochMilli());
                int update = writableDatabase.update("t_alert", values, "end_time is null", null);
                if (update != 1) {
                    Log.e(TAG, "One open case expected, but got " + update);
                }
            }
            NotificationHelper.cancel(getContext(), NotificationHelper.ALERT_NOTIFICATION_ID);
            refresh();
        });
        refresh();
        return view;
    }


    void refresh() {
        TextView textView = (TextView) view.findViewById(R.id.main_state);
        PikettState pikettState = SharedState.getPikettState(getContext());
        textView.setText(Html.fromHtml(
                "Pikett state: " + pikettState + "<br/>" +
                        "Alarm state: " + SharedState.getAlarmState(getContext()).first + "<br/>" +
                        (pikettState == PikettState.ON ? "Signal strength: " + SignalStremgthHelper.getSignalStrength(getContext()) : ""),
                Html.FROM_HTML_MODE_COMPACT)
        );
        textView.invalidate();
        Button button = (Button) view.findViewById(R.id.close_alert_button);
        button.setEnabled(SharedState.getAlarmState(getContext()).first != AlarmState.OFF);
        button.invalidate();
    }

}
