package com.github.frimtec.android.pikettassist.ui.common;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;

public class ViewPager2Helper {

  private static final String TAG = "ViewPager2Helper";

  public static void reduceDragSensitivity(ViewPager2 viewPager, int sensitivity) {
    try {
      RecyclerView recyclerView = (RecyclerView) getRecyclerViewField().get(viewPager);

      Field touchSlopField = getTouchSlopField();
      //noinspection ConstantConditions
      int touchSlop = (int) touchSlopField.get(recyclerView);
      touchSlopField.set(recyclerView, touchSlop * sensitivity);
    } catch (NoSuchFieldException|IllegalAccessException e) {
      Log.e(TAG, "Cannot change drag sensibility", e);
    }
  }

  static Field getRecyclerViewField() throws NoSuchFieldException {
    Field field = ViewPager2.class.getDeclaredField("mRecyclerView");
    field.setAccessible(true);
    return field;
  }

  static Field getTouchSlopField() throws NoSuchFieldException {
    Field field = RecyclerView.class.getDeclaredField("mTouchSlop");
    field.setAccessible(true);
    return field;
  }

}
