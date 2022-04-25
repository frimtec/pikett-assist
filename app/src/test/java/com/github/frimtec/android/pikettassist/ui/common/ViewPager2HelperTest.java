package com.github.frimtec.android.pikettassist.ui.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import androidx.recyclerview.widget.RecyclerView;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

class ViewPager2HelperTest {

  @Test
  void testFieldRecyclerViewExistsOnViewPager2() throws NoSuchFieldException {
    Field recyclerViewField = ViewPager2Helper.getRecyclerViewField();
    assertThat(recyclerViewField).isNotNull();
    assertThat(recyclerViewField.getType()).isSameAs(RecyclerView.class);
  }

  @Test
  void testFieldTouchSlopExistsOnRecyclerView() throws NoSuchFieldException {
    Field touchSlopFieldField = ViewPager2Helper.getTouchSlopField();
    assertThat(touchSlopFieldField).isNotNull();
    assertThat(touchSlopFieldField.getType()).isSameAs(int.class);
  }

}