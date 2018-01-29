package com.vmware.bifrost.bridge.util;

public class BifrostUtil {

    public static String convertTopicToChannel(String channel) {
        return channel.replaceAll("/topic/", "").trim();
    }

    public static String convertChannelToTopic(String channel) {
        return "/topic/" + channel;
    }
}
