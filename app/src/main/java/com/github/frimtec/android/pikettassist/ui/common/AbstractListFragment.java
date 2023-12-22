package com.github.frimtec.android.pikettassist.ui.common;

import android.os.Bundle;
import android.view.*;
import android.widget.ExpandableListView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter.Group;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractListFragment<K extends Comparable<K>, I> extends Fragment {

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
    listView.setOnGroupExpandListener(groupPosition -> changeExpandedGroupsPreferences(expandedGroup -> {
      expandedGroup.add(getGroup(groupPosition).key());
      return expandedGroup;
    }));
    listView.setOnGroupCollapseListener(groupPosition -> changeExpandedGroupsPreferences(expandedGroup -> {
      expandedGroup.remove(getGroup(groupPosition).key());
      return expandedGroup;
    }));

    refresh();
    return view;
  }

  protected abstract void changeExpandedGroupsPreferences(Function<Set<K>, Set<K>> transformer);

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
    getExpandedGroups().forEach(groupPosition -> {
      if (groupPosition < getGroupCount()) {
        listView.expandGroup(groupPosition);
      }
    });
    addButton.setVisibility(isAddButtonVisible() ? View.VISIBLE : View.INVISIBLE);
  }

  protected abstract Set<Integer> getExpandedGroups();

  protected boolean isAddButtonVisible() {
    return false;
  }

  protected abstract void configureListView(ExpandableListView listView);

  protected abstract AbstractExpandableListAdapter<K, I> createAdapter();

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

  public final int getGroupCount() {
    return this.listView.getExpandableListAdapter().getGroupCount();
  }

  public final Group<K, I> getGroup(int groupPosition) {
    //noinspection unchecked
    return (Group<K, I>) this.listView.getExpandableListAdapter().getGroup(groupPosition);
  }

  public final I getChild(int groupPosition, int childPosition) {
    //noinspection unchecked
    return (I) this.listView.getExpandableListAdapter().getChild(groupPosition, childPosition);
  }

}
