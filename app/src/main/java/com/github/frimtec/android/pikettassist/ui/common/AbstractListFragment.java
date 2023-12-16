package com.github.frimtec.android.pikettassist.ui.common;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Optional;

public abstract class AbstractListFragment extends Fragment {

  private ExpandableListView listView;
  private FloatingActionButton addButton;

  private final FragmentPosition fragmentPosition;

  protected AbstractListFragment(FragmentPosition fragmentPosition) {
    this.fragmentPosition = fragmentPosition;
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
    addButton = view.findViewById(R.id.list_add_button);
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
    listView.setAdapter(createAdapter());
    addButton.setVisibility(isAddButtonVisible() ? View.VISIBLE : View.INVISIBLE);
  }

  protected boolean isAddButtonVisible() {
    return false;
  }

  protected abstract void configureListView(ExpandableListView listView);

  protected abstract ExpandableListAdapter createAdapter();

  protected ExpandableListView getListView() {
    return this.listView;
  }

  @Override
  public final boolean onContextItemSelected(MenuItem item) {
    if (item.getGroupId() != fragmentPosition.ordinal()) {
      return super.onContextItemSelected(item);
    }
    return onFragmentContextItemSelected(item);
  }

  protected boolean onFragmentContextItemSelected(MenuItem item) {
    return false;
  }

  public final MenuItem addContextMenu(@NonNull ContextMenu menu, int id, @StringRes int text) {
    return menu.add(fragmentPosition.ordinal(), id, Menu.NONE, text);
  }
}
