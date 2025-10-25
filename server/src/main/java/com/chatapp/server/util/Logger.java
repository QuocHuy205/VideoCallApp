package com.chatapp.server.util;

import org.slf4j.LoggerFactory;

public class Logger {
    private static Logger instance;
    private final org.slf4j.Logger logger;

    private Logger() {
        this.logger = LoggerFactory.getLogger("ChatAppServer");
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}