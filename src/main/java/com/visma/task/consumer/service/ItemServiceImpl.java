package com.visma.task.consumer.service;

import com.visma.task.consumer.model.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    private ProcessingService processingService;

    @Autowired
    public ItemServiceImpl(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public String createItem(String content) throws Exception {
        String uiid;
        do {
            try {
                uiid = processingService.callInit(content);
            } catch (SocketException e) {
                TimeUnit.MILLISECONDS.sleep(200);
                uiid = processingService.callInit(content);
            }
        } while (uiid == null);
        return uiid;
    }

    @Override
    public StatusType getStatusType(String uiid) throws Exception {
        StatusType statusType = null;
        do {
            if (statusType != null) {
                //only for testing purposes
                TimeUnit.MILLISECONDS.sleep(200);
            }
            try {
                statusType = processingService.getStatus(uiid).getStatusType();
            } catch (SocketException e) {
                TimeUnit.MILLISECONDS.sleep(200);
                statusType = processingService.getStatus(uiid).getStatusType();
            }
            logger.info("New status for {}: {}", uiid, statusType.toString());
        } while (statusType.equals(StatusType.IN_PROGRESS));
        return statusType;
    }
}
