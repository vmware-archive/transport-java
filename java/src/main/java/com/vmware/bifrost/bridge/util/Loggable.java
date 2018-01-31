package com.vmware.bifrost.bridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loggable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getName() {
        return this.getClass().getName();
    }

    public Loggable() {

    }

    protected void logInfoMessage(String emoji, String message, String value){
        logger.info("{}  {}: " + "\u001b[1m\u001b[35;1m{}\u001b[0m", emoji, message.trim(), value.trim());
    }

    protected void logErrorMessage(String message, String value){
        logger.error("\uD83D\uDED1  \u001b[41;1m{}:\u001b[0m \u001b[31;1m{}\u001b[0m", message.trim(), value.trim());
    }

    protected void logDebugMessage(String message, String value){
        logger.debug("\uD83D\uDD39  \u001b[38;5;245m{}: \u001b[35m{}\u001b[0m", message.trim(), value.trim());
    }

    protected void logTraceMessage(String message, String value){
        logger.trace("\uD83D\uDD38  \u001b[38;5;245m{}: \u001b[38;5;67m{}\u001b[0m", message.trim(), value.trim());
    }

    protected void logWarnMessage(String message){
        logger.warn("⚠️  \u001b[33m\u001b[1m{}\u001b[0m", message.trim());
    }

    protected void logBannerMessage(String emoji, String message){
        logger.info("{}  \u001b[1m\u001b[38;5;200m{}\u001b[0m", emoji, message.trim());
    }

}
