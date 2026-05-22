package com.github.frimtec.android.pikettassist.service.dao;

import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.Photo;

import java.time.Instant;
import java.util.Set;

public record ContactCopy(
    Instant copyTimestamp,
    ContactReference reference,
    String fullName,
    String nickname,
    Photo photo,
    Set<String> phoneNumbers,
    Set<String> shortCodes
) {

}
