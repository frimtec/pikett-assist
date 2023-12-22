package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractExpandableListAdapterTest {

  record Person(String name, int age, List<? extends Person> children) implements Comparable<Person>{

    @Override
    public int compareTo(Person o) {
      return 0;
    }
  }

  static class PersonAdapter extends AbstractExpandableListAdapter<Integer, Person> {

    public PersonAdapter(List<Person> persons, Comparator<Integer> order) {
      super(Mockito.mock(Context.class), persons, Person::age, order);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
      return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
      return null;
    }
  }

  static class PersonHierarchyAdapter extends AbstractExpandableListAdapter<Person, Person> {

    public PersonHierarchyAdapter(List<? extends Person> persons) {
      super(Mockito.mock(Context.class), persons, Person::children);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
      return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
      return null;
    }
  }

  @Test
  public void forHierarchicalElement() {
    // arrange
    Person p1 = new Person("Jim", 40, Collections.emptyList());
    Person p21 = new Person("John", 12, Collections.emptyList());
    Person p22 = new Person("James", 15, Collections.emptyList());
    Person p2 = new Person("Ruben", 50, List.of(
        p21,
        p22
    ));
    List<Person> items = List.of(
        p1,
        p2
    );

    // act
    var adapter = new PersonHierarchyAdapter(items);

    // assert
    var groups = adapter.getGroupedItems();
    assertEquals(2, groups.size());
    assertThat(groups.get(0).key()).isSameAs(p1);
    assertThat(groups.get(0).items()).isEmpty();
    assertThat(groups.get(1).key()).isSameAs(p2);
    assertThat(groups.get(1).items()).isEqualTo(List.of(p21, p22));
  }

  @Test
  public void forNaturalOrder() {
    // arrange
    Person p1 = new Person("Anna", 2, Collections.emptyList());
    Person p2 = new Person("Jim", 3, Collections.emptyList());
    Person p3 = new Person("Ruben", 2, Collections.emptyList());
    List<Person> items = List.of(
        p1,
        p2,
        p3
    );

    // act
    var adapter = new PersonAdapter(items, Comparator.naturalOrder());

    // assert
    var groups = adapter.getGroupedItems();
    assertEquals(2, groups.size());
    assertThat(groups.get(0).key()).isEqualTo(2);
    assertThat(groups.get(0).items()).isEqualTo(List.of(p1, p3));
    assertThat(groups.get(1).key()).isEqualTo(3);
    assertThat(groups.get(1).items()).isEqualTo(List.of(p2));

    assertThat(adapter.getGroupCount()).isEqualTo(2);
    assertThat(adapter.getChildrenCount(0)).isEqualTo(2);
    assertThat(adapter.getChildrenCount(1)).isEqualTo(1);
    assertThat(((AbstractExpandableListAdapter.Group<?, ?>) adapter.getGroup(0)).key()).isEqualTo(2);
    assertThat(((AbstractExpandableListAdapter.Group<?, ?>) adapter.getGroup(1)).key()).isEqualTo(3);
    assertThat(adapter.getChild(0, 0)).isSameAs(p1);
    assertThat(adapter.getChild(0, 1)).isSameAs(p3);
    assertThat(adapter.getChild(1, 0)).isSameAs(p2);
  }

  @Test
  public void forReverseOrder() {
    // arrange
    Person p1 = new Person("Anna", 2, Collections.emptyList());
    Person p2 = new Person("Jim", 3, Collections.emptyList());
    Person p3 = new Person("Ruben", 2, Collections.emptyList());
    var items = List.of(
        p1,
        p2,
        p3
    );

    // act
    var adapter = new PersonAdapter(items, Comparator.reverseOrder());

    // assert
    var groups = adapter.getGroupedItems();
    assertEquals(2, groups.size());
    assertThat(groups.get(0).key()).isEqualTo(3);
    assertThat(groups.get(0).items()).isEqualTo(List.of(p2));
    assertThat(groups.get(1).key()).isEqualTo(2);
    assertThat(groups.get(1).items()).isEqualTo(List.of(p1, p3));

    assertThat(adapter.getGroupCount()).isEqualTo(2);
    assertThat(adapter.getChildrenCount(0)).isEqualTo(1);
    assertThat(adapter.getChildrenCount(1)).isEqualTo(2);
    assertThat(((AbstractExpandableListAdapter.Group<?, ?>) adapter.getGroup(0)).key()).isEqualTo(3);
    assertThat(((AbstractExpandableListAdapter.Group<?, ?>) adapter.getGroup(1)).key()).isEqualTo(2);
    assertThat(adapter.getChild(0, 0)).isSameAs(p2);
    assertThat(adapter.getChild(1, 0)).isSameAs(p1);
    assertThat(adapter.getChild(1, 1)).isSameAs(p3);
  }

  @Test
  public void ids() {
    // arrange
    Person p1 = new Person("Anna", 2, Collections.emptyList());
    Person p2 = new Person("Jim", 3, Collections.emptyList());
    Person p3 = new Person("Ruben", 2, Collections.emptyList());
    var items = List.of(
        p1,
        p2,
        p3
    );

    // act
    var adapter = new PersonAdapter(items, Comparator.naturalOrder());

    // assert
    assertThat(adapter.getGroupId(0)).isEqualTo(0);
    assertThat(adapter.getGroupId(1)).isEqualTo(1);

    assertThat(adapter.getChildId(0, 0)).isEqualTo(1_000_000);
    assertThat(adapter.getChildId(0, 1)).isEqualTo(1_000_001);
    assertThat(adapter.getChildId(1, 0)).isEqualTo(2_000_000);
  }


  @Test
  public void otherApis() {
    // act
    var adapter = new PersonAdapter(Collections.emptyList(), Comparator.naturalOrder());

    // assert
    assertThat(adapter.getContext()).isNotNull();
    assertThat(adapter.hasStableIds()).isTrue();
    IntStream.of(0, 1)
        .forEach(i -> IntStream.of(0, 1)
            .forEach(j -> assertThat(adapter.isChildSelectable(i, j)).isTrue())
        );
  }
}