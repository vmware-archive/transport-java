package com.vmware.bifrost.bridge.util;

import com.vmware.bifrost.core.util.Loggable;

public abstract class AbstractTest extends Loggable {

    protected void logTestMessage(String message, String value) {
        log.debug("\u001b[38;5;100m[test]\u001b[0m {} \u001b[38;5;99m{}\u001b[0m", message.trim(), value);
    }

    protected void logTestMessage(String message) {
        log.debug("\u001b[38;5;100m[test]\u001b[0m {} \u001b[38;5;99m{}\u001b[0m", message.trim());
    }
}
