package com.example.personal_project.domain;

import java.util.List;

public interface ItemRepository {

    CollectionItem add(CollectionItem collectionItem);
    List<CollectionItem> findAll();
    CollectionItem findById(Long id);
}
