package org.itmo.lab3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyOrganizationCreated(Long organizationId) {
        messagingTemplate.convertAndSend("/topic/organizations", 
                new NotificationMessage("created", organizationId));
    }

    public void notifyOrganizationUpdated(Long organizationId) {
        messagingTemplate.convertAndSend("/topic/organizations", 
                new NotificationMessage("updated", organizationId));
    }

    public void notifyOrganizationDeleted(Long organizationId) {
        messagingTemplate.convertAndSend("/topic/organizations", 
                new NotificationMessage("deleted", organizationId));
    }

    public static class NotificationMessage {
        private String action;
        private Long organizationId;

        public NotificationMessage(String action, Long organizationId) {
            this.action = action;
            this.organizationId = organizationId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }
    }
}

