package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;

import java.util.List;

class StateArrayAdapter extends ArrayAdapter<State> {

  StateArrayAdapter(Context context, List<State> states) {
    super(context, 0, states);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    // Get the data item for this position
    State state = getItem(position);
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.state_item, parent, false);
    }

    ((ImageView) convertView.findViewById(R.id.state_item_image)).setImageResource(state.getIconResource());

    switch (state.getState()) {
      case GREEN:
        Bitmap ledGreen = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_green);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledGreen);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledGreen);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_3)).setImageBitmap(ledGreen);
        break;
      case YELLOW:
        Bitmap ledYellow = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_yellow);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledYellow);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledYellow);
        break;
      case RED:
        Bitmap ledRed = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_red);
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledRed);
        break;
    }

    TextView titleView = convertView.findViewById(R.id.state_item_title);
    titleView.setText(state.getTitle());

    TextView valueView = convertView.findViewById(R.id.state_item_value);
    valueView.setText(state.getValue());

    Button button = state.getButton();
    if (button != null) {
      ViewGroup layout = convertView.findViewById(R.id.state_item_layout);
      layout.addView(button);
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
      params.addRule(RelativeLayout.BELOW, R.id.state_item_value);
      params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_BOTTOM, RelativeLayout.TRUE);

      params = (RelativeLayout.LayoutParams) valueView.getLayoutParams();
      params.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.CENTER_VERTICAL, 0);
    }

    return convertView;
  }
}
