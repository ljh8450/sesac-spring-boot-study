package com.example.personal_project.presentation;

import com.example.personal_project.application.ItemService;
import com.example.personal_project.domain.CollectionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ItemController {

    ItemService itemService;

    @Autowired
    ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/items")
    public String registerItem(@RequestBody ItemDto itemDto) {
        itemService.add(itemDto);
        return "등록";
    }

    @GetMapping("/items")
    public List<ItemDto> findAllItems() {
        return itemService.findAll();
    }

    @GetMapping("/items/{id}")
    public CollectionItem findItemById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @GetMapping("/recommend")
    public List<CollectionItem> recommendItems(@RequestParam("budget") int budget) {
        return itemService.recommendItems(budget);
    }
}
