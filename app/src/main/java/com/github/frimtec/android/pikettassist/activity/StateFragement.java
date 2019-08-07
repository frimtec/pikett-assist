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
        TextView pikettState = (TextView) view.findViewById(R.id.main_state_pikett_state);
        TextView alertState = (TextView) view.findViewById(R.id.main_state_alert_state);
        TextView signalStrength = (TextView) view.findViewById(R.id.main_state_signal_strength);
        PikettState pikettStateValue = SharedState.getPikettState(getContext());
        boolean superviseSignalStrength = SharedState.getSuperviseSignalStrength(getContext());
        pikettState.setText("Pikett state: " + pikettStateValue);
        alertState.setText("Alarm state: " + SharedState.getAlarmState(getContext()).first);
        signalStrength.setText(superviseSignalStrength ? (pikettStateValue == PikettState.ON ? "Signal strength: " + SignalStremgthHelper.getSignalStrength(getContext()) : "Signal strength supervision: ENABLED") : "Signal strength supervision: DISABLED");
        pikettState.invalidate();
        Button button = (Button) view.findViewById(R.id.close_alert_button);
        button.setEnabled(SharedState.getAlarmState(getContext()).first != AlarmState.OFF);
        button.invalidate();
    }

}
