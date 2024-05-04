package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContactReferenceTest {

  @Test
  void fromSerializedStringForCompleteReference() {
    ContactReference reference = ContactReference.fromSerializedString("12;key");
    assertThat(reference.isComplete()).isTrue();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEqualTo("key");
  }

  @Test
  void fromSerializedStringForIncompleteReferenceNoSeparator() {
    ContactReference reference = ContactReference.fromSerializedString("12");
    assertThat(reference.isComplete()).isFalse();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEmpty();
  }

  @Test
  void fromSerializedStringForIncompleteReferenceEmptyLookupKey() {
    ContactReference reference = ContactReference.fromSerializedString("12;");
    assertThat(reference.isComplete()).isFalse();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEmpty();
  }

  @Test
  void gettersForCompleteReference() {
    ContactReference reference = new ContactReference(12, "key");
    assertThat(reference.isComplete()).isTrue();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEqualTo("key");
  }

  @Test
  void gettersForNullLookupKey() {
    ContactReference reference = new ContactReference(12, null);
    assertThat(reference.isComplete()).isFalse();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEmpty();
  }

  @Test
  void gettersForEmptyLookupKey() {
    ContactReference reference = new ContactReference(12, "");
    assertThat(reference.isComplete()).isFalse();
    assertThat(reference.id()).isEqualTo(12);
    assertThat(reference.lookupKey()).isEmpty();
  }

  @Test
  void gettersForNoSelection() {
    ContactReference reference = ContactReference.NO_SELECTION;
    assertThat(reference.isComplete()).isTrue();
    assertThat(reference.id()).isEqualTo(-1);
    assertThat(reference.lookupKey()).isEqualTo("NOT_SET");
  }

  @Test
  void getSerializedStringForCompleteReference() {
    ContactReference reference = new ContactReference(12, "key");
    assertThat(reference.getSerializedString()).isEqualTo("12;key");
  }

  @Test
  void getSerializedStringForIncompleteReference() {
    ContactReference reference = new ContactReference(12, null);
    assertThat(reference.getSerializedString()).isEqualTo("12;");
  }

  @Test
  void equalsForSameReference() {
    ContactReference reference = new ContactReference(12, "key");
    //noinspection EqualsWithItself
    assertThat(reference).isEqualTo(reference);
  }

  @Test
  void equalsForOtherClass() {
    ContactReference reference = new ContactReference(12, "key");
    //noinspection AssertBetweenInconvertibleTypes
    assertThat(reference).isNotEqualTo("any");
  }

  @Test
  void equalsForEqualReference() {
    ContactReference reference1 = new ContactReference(12, "key");
    ContactReference reference2 = new ContactReference(12, "key");
    assertThat(reference1).isEqualTo(reference2);
    assertThat(reference2).isEqualTo(reference1);
  }

  @Test
  void equalsForDifferentId() {
    ContactReference reference1 = new ContactReference(12, "key");
    ContactReference reference2 = new ContactReference(13, "key");
    assertThat(reference1).isNotEqualTo(reference2);
    assertThat(reference2).isNotEqualTo(reference1);
  }

  @Test
  void equalsForDifferentLookupKey() {
    ContactReference reference1 = new ContactReference(12, "key1");
    ContactReference reference2 = new ContactReference(12, "key2");
    assertThat(reference1).isNotEqualTo(reference2);
    assertThat(reference2).isNotEqualTo(reference1);
  }

  @Test
  void hashCodeForSameReference() {
    ContactReference reference = new ContactReference(12, "key");
    assertThat(reference.hashCode()).isEqualTo(reference.hashCode());
  }

  @Test
  void hashCodeForEqualReference() {
    ContactReference reference1 = new ContactReference(12, "key");
    ContactReference reference2 = new ContactReference(12, "key");
    assertThat(reference1.hashCode()).isEqualTo(reference2.hashCode());
    assertThat(reference2.hashCode()).isEqualTo(reference1.hashCode());
  }

  @Test
  void hashCodeForDifferentId() {
    ContactReference reference1 = new ContactReference(12, "key");
    ContactReference reference2 = new ContactReference(13, "key");
    assertThat(reference1.hashCode()).isNotEqualTo(reference2.hashCode());
    assertThat(reference2.hashCode()).isNotEqualTo(reference1.hashCode());
  }

  @Test
  void hashCodeForDifferentLookupKey() {
    ContactReference reference1 = new ContactReference(12, "key1");
    ContactReference reference2 = new ContactReference(12, "key2");
    assertThat(reference1.hashCode()).isNotEqualTo(reference2.hashCode());
    assertThat(reference2.hashCode()).isNotEqualTo(reference1.hashCode());
  }
}