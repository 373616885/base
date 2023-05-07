package com.qin.demo.listener;

import org.springframework.context.ApplicationEvent;

public class NotifyEvent extends ApplicationEvent {

    private String email;

    private String content;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public NotifyEvent(Object source) {
        super(source);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
