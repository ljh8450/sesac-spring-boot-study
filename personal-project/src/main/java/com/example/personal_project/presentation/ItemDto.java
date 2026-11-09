package com.example.personal_project.presentation;

import com.example.personal_project.domain.CollectionItem;

public record ItemDto(Long id, String name, Integer price) {
    public static ItemDto from(CollectionItem collectionItem) {
        return new ItemDto(
                collectionItem.id(),
                collectionItem.name(),
                collectionItem.price()
        );
    }
}
