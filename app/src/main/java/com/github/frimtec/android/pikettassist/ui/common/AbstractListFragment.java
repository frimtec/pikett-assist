package com.github.frimtec.android.pikettassist.ui.common;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.FragmentName;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Optional;

public abstract class AbstractListFragment<T> extends Fragment {

  private final FragmentName fragmentName;
  private ListView listView;

  public AbstractListFragment(FragmentName fragmentName) {
    this.fragmentName = fragmentName;
  }

  public FragmentName getFragmentName() {
    return fragmentName;
  }

  @Override
  public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list, container, false);
    SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.fragment_list_pull_to_request);
    pullToRefresh.setOnRefreshListener(() -> {
      refresh();
      pullToRefresh.setRefreshing(false);
    });

    listView = view.findViewById(R.id.fragment_list_list);


    FloatingActionButton addButton = view.findViewById(R.id.list_add_button);
    addAction().map(onClickListener -> {
      addButton.setVisibility(View.VISIBLE);
      addButton.setOnClickListener(onClickListener);
      return true;
    });

    configureListView(listView);
    refresh();
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
  }

  protected Optional<View.OnClickListener> addAction() {
    return Optional.empty();
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

  protected void switchFragment(FragmentName fragment) {
    ((MainActivity)getActivity()).switchFragment(fragment);
  }
}
