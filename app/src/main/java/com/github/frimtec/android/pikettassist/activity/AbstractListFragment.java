package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.frimtec.android.pikettassist.R;

abstract class AbstractListFragment<T> extends Fragment {

  private ListView listView;

  @Override
  public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list, container, false);
    listView = view.findViewById(R.id.activity_list);
    configureListView(listView);
    refresh();
    return view;
  }

  public final void refresh() {
    ArrayAdapter<T> adapter = createAdapter();
    listView.setAdapter(adapter);
  }

  protected abstract void configureListView(ListView listView);

  protected abstract ArrayAdapter<T> createAdapter();

  protected ListView getListView() {
    return listView;
  }
}