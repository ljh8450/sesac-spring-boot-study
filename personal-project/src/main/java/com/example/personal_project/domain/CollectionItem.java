package com.example.personal_project.domain;

import com.example.personal_project.presentation.ItemDto;

public record CollectionItem(Long id, String name, Integer price) {

    public CollectionItem(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static CollectionItem from(ItemDto itemDto) {
        return new CollectionItem(
                itemDto.id(),
                itemDto.name(),
                itemDto.price()
        );
    }
}
