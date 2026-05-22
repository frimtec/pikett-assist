package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import android.net.Uri;

import org.junit.jupiter.api.Test;

class ContactPersonTest {

  @Test
  void getNickname() {
    final String nickname = "nickname";
    ContactPerson contactPerson = new ContactPerson(nickname, 12L, "fullName", null, true);
    assertThat(contactPerson.getNickname()).isEqualTo(nickname);
  }

  @Test
  void getContactId() {
    long id = 12L;
    ContactPerson contactPerson = new ContactPerson("nickname", id, "fullName", null, true);
    assertThat(contactPerson.getContactId()).isEqualTo(id);
  }

  @Test
  void getFullName() {
    String fullName = "fullName";
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, fullName, null, true);
    assertThat(contactPerson.getFullName()).isEqualTo(fullName);
  }

  @Test
  void photoThumbnailUri() {
    String photoUri = "http://photo.uri";
    String photoThumbnailUri = "http://photo.thumbnail.uri";
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, "fullName", new Photo(photoUri, photoThumbnailUri), true);
    assertThat(contactPerson.photo().uri()).isEqualTo(Uri.parse(photoUri));
    assertThat(contactPerson.photo().thumbnailUri()).isEqualTo(Uri.parse(photoThumbnailUri));
  }

  @Test
  void getPhotoThumbnailUriForNull() {
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, "fullName", new Photo(), true);
    assertThat(contactPerson.photo().uri()).isNull();
    assertThat(contactPerson.photo().thumbnailUri()).isNull();
  }

  @Test
  void isValidForInvalidContactPerson() {
    ContactPerson contactPerson = new ContactPerson("nickname", true);
    assertThat(contactPerson.isValid()).isFalse();
  }

  @Test
  void isValidForContactPerson() {
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, "fullName", null, true);
    assertThat(contactPerson.isValid()).isTrue();
  }
}