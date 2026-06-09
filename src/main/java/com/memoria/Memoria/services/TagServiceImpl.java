package com.memoria.Memoria.services;
import com.memoria.Memoria.models.Tag;
import com.memoria.Memoria.repositories.TagRepository;
import org.springframework.stereotype.Service ;
import lombok.* ;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public Tag createTag(String name) {

        return tagRepository.save(
                new Tag(null, name, new HashSet<>())
        );
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
