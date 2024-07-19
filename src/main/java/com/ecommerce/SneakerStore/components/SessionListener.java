package com.ecommerce.SneakerStore.components;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@WebListener
@Component
public class SessionListener implements HttpSessionListener {
    private final AtomicInteger activeSessions;

    public SessionListener() {
        this.activeSessions = new AtomicInteger();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.decrementAndGet();
    }

    public int getActiveSessions() {
        return activeSessions.get();
    }
}
