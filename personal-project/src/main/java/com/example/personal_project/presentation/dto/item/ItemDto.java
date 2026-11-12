package com.example.personal_project.presentation.dto.item;

import com.example.personal_project.domain.item.model.CollectionItem;

public record ItemDto(Long id, String name, Long price, Integer stock) {
    public static ItemDto from(CollectionItem collectionItem) {
        return new ItemDto(
                collectionItem.id(),
                collectionItem.name(),
                collectionItem.price(),
                collectionItem.stock()
        );
    }
}
