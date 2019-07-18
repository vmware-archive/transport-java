package com.vmware.bifrost.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loggable {

    protected final Logger log;

    public String getName() {
        return this.getClass().getName();
    }

    public Loggable() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    public void logInfoMessage(String emoji, String message, String value){
        log.info("{}  {}: " + "\u001b[1m\u001b[35;1m{}\u001b[0m", emoji, message.trim(), value.trim());
    }

    public void logErrorMessage(String message, String value){
        log.error("\uD83D\uDED1  \u001b[41;1m{}:\u001b[0m \u001b[31;1m{}\u001b[0m", message.trim(), value.trim());
    }

    public void logDebugMessage(String message, String value){
        log.debug("\uD83D\uDD39  \u001b[38;5;245m{}: \u001b[35m{}\u001b[0m", message.trim(), value.trim());
    }

    public void logDebugMessage(String message){
        log.debug("\uD83D\uDD39  \u001b[38;5;245m{}\u001b[0m", message.trim());
    }

    public void logTraceMessage(String message, String value){
        log.trace("\uD83D\uDD38  \u001b[38;5;245m{}: \u001b[38;5;67m{}\u001b[0m", message.trim(), value.trim());
    }

    public void logWarnMessage(String message){
        log.warn("⚠️  \u001b[33m\u001b[1m{}\u001b[0m", message.trim());
    }

    public void logBannerMessage(String emoji, String message){
        log.info("{}  \u001b[1m\u001b[38;5;200m{}\u001b[0m", emoji, message.trim());
    }

}
