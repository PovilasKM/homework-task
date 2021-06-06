package com.visma.task.consumer.service;

import com.visma.task.consumer.model.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketException;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ProcessingService processingService;

    private final int REQUEST_SLEEP_TIMER;

    @Autowired
    public ItemServiceImpl(@Value("${request.sleep.timer.ms}") int requestSleepTimer, ProcessingService processingService) {
        this.REQUEST_SLEEP_TIMER = requestSleepTimer;
        this.processingService = processingService;
    }

    @Override
    public String createItem(String content) throws Exception {
        String uiid;
        do {
            try {
                uiid = processingService.callInit(content);
            } catch (SocketException e) {
                logger.warn("SocketException while calling thirdparty service: {}", e.getMessage());
                // In case third party service load is too big right now
                TimeUnit.MILLISECONDS.sleep(REQUEST_SLEEP_TIMER);
                uiid = processingService.callInit(content);
            }
        } while (uiid == null);
        return uiid;
    }

    @Override
    public StatusType getStatusType(String uuid) throws Exception {
        StatusType statusType = null;
        do {
            if (statusType != null) {
                // thirdparty service is slot, no need to ping it instantly
                TimeUnit.MILLISECONDS.sleep(REQUEST_SLEEP_TIMER);
            }
            try {
                statusType = processingService.getStatus(uuid).getStatusType();
            } catch (SocketException e) {
                logger.warn("SocketException while calling thirdparty service: {}", e.getMessage());
                // In case third party service load is too big right now
                TimeUnit.MILLISECONDS.sleep(REQUEST_SLEEP_TIMER);
                statusType = processingService.getStatus(uuid).getStatusType();
            }
        } while (statusType.equals(StatusType.IN_PROGRESS));
        return statusType;
    }
}
