package com.example.personal_project.application;

import com.example.personal_project.domain.CollectionItem;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.presentation.ItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemDto add(ItemDto itemDto) {
        if (itemDto == null
                || itemDto.name() == null
                || itemDto.name().isBlank()) {
            throw new IllegalArgumentException("상품명을 입력해야 합니다.");
        }

        if (itemDto.price() == null || itemDto.price() < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        CollectionItem collectionItem = new CollectionItem(
                null,
                itemDto.name().trim(),
                itemDto.price()
        );
        CollectionItem savedItem = itemRepository.add(collectionItem);
        ItemDto savedItemDto = ItemDto.from(savedItem);
        return savedItemDto;
    }

    public List<ItemDto> findAll() {
        List<CollectionItem> items = itemRepository.findAll();
        List<ItemDto> itemDtos = items.stream()
                .map(collectionItem -> ItemDto.from(collectionItem))
                .toList();
        return itemDtos;
    }

    public CollectionItem findById(Long id) {
        CollectionItem item = itemRepository.findById(id);
        return item;
    }

    public List<CollectionItem> recommendItems(int budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0보다 커야 합니다.");
        }

        List<CollectionItem> recommend = itemRepository.findAll().stream()
                .filter(item -> item.price() <= budget)
                .sorted(Comparator.comparing(CollectionItem::price))
                .limit(3)
                .toList();
        return recommend;
    }
}
