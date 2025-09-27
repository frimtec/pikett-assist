package com.github.frimtec.android.pikettassist.ui.overview;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;
import com.github.frimtec.android.pikettassist.ui.common.ImageHelper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class StateExpandableListAdapter extends AbstractExpandableListAdapter<State, State> {

  private final Map<State.TrafficLight, Bitmap> ledBitmaps = new EnumMap<>(State.TrafficLight.class);

  StateExpandableListAdapter(Context context, List<? extends State> states) {
    super(
        context,
        states,
        State::getChildStates
    );

    ledBitmaps.put(OFF, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_grey));
    ledBitmaps.put(GREEN, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_green));
    ledBitmaps.put(YELLOW, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_yellow));
    ledBitmaps.put(RED, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_red));
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    return getView(convertView, parent, getGroupedItems().get(groupPosition).key(), false);
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    return getView(convertView, parent, getGroupedItems().get(groupPosition).items().get(childPosition), true);
  }

  private View getView(View convertView, ViewGroup parent, State state, boolean child) {
    Objects.requireNonNull(state);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.state_item, parent, false);
    }

    if (!child && !state.getChildStates().isEmpty()) {
      convertView.setBackgroundColor(getContext().getColor(R.color.tableGroupBackground));
    } else if (child) {
      convertView.setPadding(dpToPx(20), 0, dpToPx(10), 0);
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

    ImageView imageView = convertView.findViewById(R.id.state_item_value_image);
    TextView imageText = convertView.findViewById(R.id.state_item_image_text);
    ImageHelper.uriToBitmap(getContext().getContentResolver(), state.getValueImage()).ifPresentOrElse(
        bitmap -> {
          imageView.setImageBitmap(bitmap);
          imageView.setVisibility(VISIBLE);
          imageText.setText(state.getValue());
          imageText.setVisibility(VISIBLE);

          valueView.setVisibility(INVISIBLE);
        },
        () -> {
          valueView.setText(state.getValue());
          valueView.setVisibility(VISIBLE);

          imageView.setVisibility(INVISIBLE);
          imageText.setVisibility(INVISIBLE);
        }
    );
    return convertView;
  }

  int dpToPx(int dp) {
    float scale = getContext().getResources().getDisplayMetrics().density;
    return Math.round(dp * scale + 0.5f);
  }
}
