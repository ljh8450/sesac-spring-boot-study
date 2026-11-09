package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.CollectionItem;
import com.example.personal_project.domain.ItemRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ListItemRepository implements ItemRepository {

    private List<CollectionItem> collectionItems = new ArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        CollectionItem savedItem = new CollectionItem(sequence.getAndAdd(1L), collectionItem.name(), collectionItem.price());

        collectionItems.add(savedItem);
        return savedItem;
    }


    @Override
    public List<CollectionItem> findAll() {
        return collectionItems;
    }

    @Override
    public CollectionItem findById(Long id) {
        return collectionItems.stream()
                .filter(collectionItem -> collectionItem.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
