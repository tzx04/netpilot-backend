package com.netpilot.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IcmpUtil {

    /**
     * Ping 指定 IP 地址
     * @param ipAddress 目标 IP
     * @param timeout 超时时间（毫秒）
     * @return true 表示在线，false 表示离线
     */
    public static boolean ping(String ipAddress, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            // isReachable 在 Windows 使用 ICMP，Linux 需要 root 权限时会降级到 TCP Echo
            return address.isReachable(timeout);
        } catch (UnknownHostException e) {
            // IP 格式错误
            return false;
        } catch (IOException e) {
            // 网络异常
            return false;
        }
    }

    /**
     * Ping 指定 IP（默认超时 3000ms）
     */
    public static boolean ping(String ipAddress) {
        return ping(ipAddress, 3000);
    }
}