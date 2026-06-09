package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Tag;

import java.util.List;

public interface TagService {

    Tag createTag(String name);

    List<Tag> getAllTags();
}
