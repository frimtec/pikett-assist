package com.github.frimtec.android.pikettassist.ui.common;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    doOnCreate(savedInstanceState);
    EdgeToEdgeHelper.handleInsets(findViewById(android.R.id.content));
  }

  protected abstract void doOnCreate(Bundle savedInstanceState);
}
