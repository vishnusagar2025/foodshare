package com.example.foodshare.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Console notifications; swap the body for real email/SMS later. */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger("NOTIFY");
    public void send(String to, String message) { log.info("To {}: {}", to, message); }
}
