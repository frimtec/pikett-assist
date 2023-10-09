package com.github.frimtec.android.pikettassist.ui.overview;

import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class StateArrayAdapter extends ArrayAdapter<State> {

  private final Map<State.TrafficLight, Bitmap> ledBitmaps = new EnumMap<>(State.TrafficLight.class);

  StateArrayAdapter(Context context, List<State> states) {
    super(context, 0, states);
    ledBitmaps.put(OFF, BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_grey));
    ledBitmaps.put(GREEN, BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_green));
    ledBitmaps.put(YELLOW, BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_yellow));
    ledBitmaps.put(RED, BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_circle_red));
  }


  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    State state = getItem(position);
    Objects.requireNonNull(state);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.state_item, parent, false);
    }

    ((ImageView) convertView.findViewById(R.id.state_item_image)).setImageResource(state.getIconResource());

    switch (state.getState()) {
      case GREEN -> {
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledBitmaps.get(GREEN));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledBitmaps.get(GREEN));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_3)).setImageBitmap(ledBitmaps.get(GREEN));
      }
      case YELLOW -> {
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledBitmaps.get(YELLOW));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledBitmaps.get(YELLOW));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_3)).setImageBitmap(ledBitmaps.get(OFF));
      }
      case RED -> {
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledBitmaps.get(RED));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledBitmaps.get(OFF));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_3)).setImageBitmap(ledBitmaps.get(OFF));
      }
      default -> {
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_1)).setImageBitmap(ledBitmaps.get(OFF));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_2)).setImageBitmap(ledBitmaps.get(OFF));
        ((ImageView) convertView.findViewById(R.id.state_traffic_light_3)).setImageBitmap(ledBitmaps.get(OFF));
      }
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
