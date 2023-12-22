package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;
import android.widget.BaseExpandableListAdapter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractExpandableListAdapter<K extends Comparable<K>, I> extends BaseExpandableListAdapter {

  public record Group<K extends Comparable<K>, I>(K key, List<? extends I> items) {

  }

  private final Context context;
  private final List<Group<K, ? extends I>> groupedItems;

  public AbstractExpandableListAdapter(Context context, List<I> items, Function<I, K> groupingFunction, Comparator<K> order) {
    this.context = context;
    Map<K, List<I>> groupedItems = items.stream()
        .collect(Collectors.groupingBy(groupingFunction));
    this.groupedItems = groupedItems.keySet()
        .stream()
        .sorted(order)
        .map(key -> new Group<>(key, groupedItems.get(key)))
        .collect(Collectors.toList());
  }

  public AbstractExpandableListAdapter(Context context, List<? extends K> items, Function<K, List<? extends I>> childFunction) {
    this.context = context;
    this.groupedItems = items.stream()
        .map(key -> new Group<>(key, childFunction.apply(key)))
        .collect(Collectors.toList());
  }

  protected final Context getContext() {
    return context;
  }

  protected final List<Group<K, ? extends I>> getGroupedItems() {
    return groupedItems;
  }

  @Override
  public final int getGroupCount() {
    return this.groupedItems.size();
  }

  @Override
  public final int getChildrenCount(int groupPosition) {
    return this.groupedItems.get(groupPosition).items().size();
  }

  @Override
  public final Object getGroup(int groupPosition) {
    return this.groupedItems.get(groupPosition);
  }

  @Override
  public final Object getChild(int groupPosition, int childPosition) {
    return this.groupedItems.get(groupPosition).items().get(childPosition);
  }

  @Override
  public final long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public final long getChildId(int groupPosition, int childPosition) {
    return (groupPosition + 1) * 1_000_000L + childPosition;
  }

  @Override
  public final boolean hasStableIds() {
    return true;
  }

  @Override
  public final boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
