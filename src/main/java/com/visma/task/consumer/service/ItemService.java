package com.visma.task.consumer.service;

import com.visma.task.consumer.model.Status;
import com.visma.task.consumer.model.StatusType;

public interface ItemService {
    String createItem(String content) throws Exception;
    StatusType getStatusType(String uuid) throws Exception;
}
