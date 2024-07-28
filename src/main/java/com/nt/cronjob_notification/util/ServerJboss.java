package com.nt.cronjob_notification.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerJboss {

    public static String getServerIP() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }


}
