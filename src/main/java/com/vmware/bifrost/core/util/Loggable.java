package com.vmware.bifrost.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loggable {

    protected final Logger log;
    protected boolean useJazz = false;

    public String getName() {
        return this.getClass().getName();
    }

    public Loggable() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    public void enableLoggingJazz() {
        this.useJazz = true;
    }

    public void disableLoggingJazz() {
        this.useJazz = false;
    }

    public void logInfoMessage(String emoji, String message, String value) {
        if (useJazz) {
            log.info("{}  {}: " + "\u001b[1m\u001b[35;1m{}\u001b[0m", emoji, message.trim(), value.trim());
        } else {
            log.info("{}  {}: ", message.trim(), value.trim());
        }
    }

    public void logErrorMessage(String message, String value) {
        if (useJazz) {
            log.error("\uD83D\uDED1  \u001b[41;1m{}:\u001b[0m \u001b[31;1m{}\u001b[0m", message.trim(), value.trim());
        } else {
            log.error("{}: {}", message.trim(), value.trim());
        }
    }

    public void logDebugMessage(String message, String value) {
        if (useJazz) {
            log.debug("\uD83D\uDD39  \u001b[38;5;245m{}: \u001b[35m{}\u001b[0m", message.trim(), value.trim());
        } else {
            log.debug("{}: {}", message.trim(), value.trim());
        }
    }

    public void logDebugMessage(String message) {
        if (useJazz) {
            log.debug("\uD83D\uDD39  \u001b[38;5;245m{}\u001b[0m", message.trim());
        } else {
            log.debug("{}", message.trim());
        }
    }

    public void logTraceMessage(String message, String value) {
        if (useJazz) {
            log.trace("\uD83D\uDD38  \u001b[38;5;245m{}: \u001b[38;5;67m{}\u001b[0m", message.trim(), value.trim());
        } else {
            log.trace("{}: {}", message.trim(), value.trim());
        }
    }

    public void logWarnMessage(String message) {
        if (useJazz) {
            log.warn("⚠️  \u001b[33m\u001b[1m{}\u001b[0m", message.trim());
        } else {
            log.warn("{}", message.trim());
        }
    }

    public void logBannerMessage(String emoji, String message) {
        if (useJazz) {
            log.info("{}  \u001b[1m\u001b[38;5;200m{}\u001b[0m", emoji, message.trim());
        } else {
            log.info("{} {}", emoji, message.trim());
        }
    }
}
