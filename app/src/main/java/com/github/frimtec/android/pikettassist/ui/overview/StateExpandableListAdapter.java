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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class StateExpandableListAdapter extends BaseExpandableListAdapter {

  private final Map<State.TrafficLight, Bitmap> ledBitmaps = new EnumMap<>(State.TrafficLight.class);
  private final Context context;
  private final List<State> states;

  StateExpandableListAdapter(Context context, List<State> states) {
    this.context = context;
    this.states = states;
    ledBitmaps.put(OFF, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_grey));
    ledBitmaps.put(GREEN, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_green));
    ledBitmaps.put(YELLOW, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_yellow));
    ledBitmaps.put(RED, BitmapFactory.decodeResource(context.getResources(), R.drawable.led_circle_red));
  }

  @Override
  public int getGroupCount() {
    return this.states.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return this.states.get(groupPosition).getChildStates().size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.states.get(groupPosition);
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return this.states.get(groupPosition).getChildStates().get(childPosition);
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return groupPosition * 1_000_000L + childPosition;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    return getView(convertView, parent, this.states.get(groupPosition), false);
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    return getView(convertView, parent, this.states.get(groupPosition).getChildStates().get(childPosition), true);
  }

  private View getView(View convertView, ViewGroup parent, State state, boolean child) {
    Objects.requireNonNull(state);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.state_item, parent, false);
    }

    if (!child && !state.getChildStates().isEmpty()) {
      convertView.setBackgroundColor(context.getColor(R.color.tableGroupBackground));
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

  int dpToPx(int dp) {
    float scale = context.getResources().getDisplayMetrics().density;
    return Math.round(dp * scale + 0.5f);
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
