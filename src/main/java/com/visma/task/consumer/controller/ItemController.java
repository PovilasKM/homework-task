package com.visma.task.consumer.controller;

import com.visma.task.consumer.model.Item;
import com.visma.task.consumer.model.Status;
import com.visma.task.consumer.model.StatusType;
import com.visma.task.consumer.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/items", produces = "application/json")
public class ItemController {
    //TODO create controller advice

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/{content}")
    public ResponseEntity getItemStatus(@PathVariable String content) throws Exception {
        String uiid = itemService.createItem(content);
        StatusType statusType;
        do {
            statusType = itemService.getStatusType(uiid);
            if (statusType.equals(StatusType.FAILED)) {
                uiid = itemService.createItem(content);
            }
        } while (!statusType.equals(StatusType.OK));

        return ResponseEntity.ok().build();
    }
}
