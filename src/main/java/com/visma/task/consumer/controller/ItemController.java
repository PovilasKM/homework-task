package com.visma.task.consumer.controller;

import com.visma.task.consumer.model.Item;
import com.visma.task.consumer.model.StatusType;
import com.visma.task.consumer.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/items", produces = "application/json")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/{content}")
    public ResponseEntity<Item> getItemStatus(@PathVariable String content) throws Exception {
        String uuid = itemService.createItem(content);
        StatusType statusType;
        do {
            statusType = itemService.getStatusType(uuid);
            if (statusType.equals(StatusType.FAILED)) {
                uuid = itemService.createItem(content);
            }
        } while (!statusType.equals(StatusType.OK));

        return ResponseEntity.ok().body(new Item(content, StatusType.OK));
    }
}
