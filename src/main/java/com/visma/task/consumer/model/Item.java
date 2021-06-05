package com.visma.task.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Item {

    private String content;
    private StatusType status;
}
