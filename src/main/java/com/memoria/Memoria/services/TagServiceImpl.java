package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Tag;
import com.memoria.Memoria.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Tag findOrCreateByName(String name) {
        String normalizedName = normalizeTagName(name);
        return tagRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> tagRepository.save(new Tag(null, normalizedName, new LinkedHashSet<>())));
    }

    @Override
    @Transactional
    public Set<Tag> resolveTagsFromInput(String tagInput) {
        if (tagInput == null || tagInput.isBlank()) {
            return new LinkedHashSet<>();
        }

        Set<String> uniqueNames = new LinkedHashSet<>();
        for (String part : tagInput.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                uniqueNames.add(normalizeTagName(trimmed));
            }
        }

        Set<Tag> tags = new LinkedHashSet<>();
        for (String name : uniqueNames) {
            tags.add(findOrCreateByName(name));
        }
        return tags;
    }

    @Override
    public String formatTagsForInput(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        List<String> names = new ArrayList<>();
        tags.stream()
                .map(Tag::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(names::add);
        return String.join(", ", names);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    private String normalizeTagName(String name) {
        return name.trim();
    }
}
