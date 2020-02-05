package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.frimtec.android.pikettassist.R;
import com.takisoft.preferencex.EditTextPreference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpTextPreference extends EditTextPreference {

  private static final int NO_GROUP_COUNT_CHECK = -1;

  private int maxGroups = NO_GROUP_COUNT_CHECK;

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context) {
    super(context);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    configureAttributes(context, attrs, 0);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    configureAttributes(context, attrs, defStyleAttr);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    configureAttributes(context, attrs, defStyleAttr);
    setupView(context);
  }

  private void configureAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RegExpTextPreference, defStyleAttr, 0);
    maxGroups = a.getInteger(R.styleable.RegExpTextPreference_max_groups, NO_GROUP_COUNT_CHECK);
    a.recycle();
  }

  private void setupView(Context context) {
    setOnBindEditTextListener(editText -> {
      setupHelpButton(context, editText);
      setupValidator(context, editText);
    });
  }

  private void setupValidator(Context context, EditText editText) {
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        try {
          Pattern pattern = Pattern.compile(s.toString());
          if (maxGroups > NO_GROUP_COUNT_CHECK) {
            Matcher matcher = pattern.matcher("");
            if (matcher.groupCount() > maxGroups) {
              editText.setError(String.format(context.getString(R.string.error_regexp_groups), RegExpTextPreference.this.maxGroups));
            }
          }
        } catch (PatternSyntaxException e) {
          editText.setError(context.getString(R.string.error_regexp_pattern));
        }
      }
    });
  }

  private void setupHelpButton(Context context, EditText editText) {
    ViewGroup rootView = (ViewGroup) editText.getRootView();
    ViewGroup viewGroup = (ViewGroup) rootView.getChildAt(0);
    if (viewGroup != null) {
      Button helpButton = new Button(context);
      helpButton.setText(R.string.button_regexp_help);
      helpButton.setOnClickListener(v -> {
        String url = "https://en.wikipedia.org/wiki/Regular_expression";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
      });
      viewGroup.addView(helpButton);
    }
  }
}
