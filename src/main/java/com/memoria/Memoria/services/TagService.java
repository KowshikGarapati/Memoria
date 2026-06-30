package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {

    Tag findOrCreateByName(String name);

    Set<Tag> resolveTagsFromInput(String tagInput);

    String formatTagsForInput(Set<Tag> tags);

    List<Tag> getAllTags();
}
