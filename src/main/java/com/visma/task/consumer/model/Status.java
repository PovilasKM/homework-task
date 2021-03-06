package com.visma.task.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Status {

    private String uuid;
    private StatusType statusType;
}
