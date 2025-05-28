package com.island.network;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import com.island.view.ActionLogView;

public class BroadcastAddressCalculator {
    private static final String FALLBACK_BROADCAST = "255.255.255.255";
    private static ActionLogView logView; // 可以通过静态方法设置

    private BroadcastAddressCalculator() {
        throw new AssertionError("工具类禁止实例化");
    }

    /**
     * 设置日志视图
     * @param view 日志视图实例
     */
    public static void setLogView(ActionLogView view) {
        logView = view;
    }

    private static void log(String message) {
        if (logView != null) {
            logView.warning(message);
        } else {
            System.err.println(message);
        }
    }

    public static class NetworkInterfaceException extends Exception {
        public NetworkInterfaceException(String message) {
            super(message);
        }

        public NetworkInterfaceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 获取所有有效的IPv4广播地址
     * @return 广播地址字符串集合（如["192.168.1.255", "192.168.255.255"]）
     * @throws NetworkInterfaceException 当无法获取任何有效的网络接口时
     */
    public static Set<String> getBroadcastAddresses() throws NetworkInterfaceException {
        Set<String> broadcastAddresses = new HashSet<>();
        boolean hasPermissionIssue = false;
        boolean hasAnyValidInterface = false;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                throw new NetworkInterfaceException("无法获取网络接口列表");
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                try {
                    // 检查接口是否有效
                    if (!isValidInterface(ni)) {
                        continue;
                    }
                    
                    hasAnyValidInterface = true;
                    log("找到有效网络接口: " + ni.getDisplayName());
                    
                    Set<String> interfaceBroadcasts = getBroadcastAddresses(ni);
                    if (!interfaceBroadcasts.isEmpty()) {
                        log("接口 " + ni.getDisplayName() + " 的广播地址: " + interfaceBroadcasts);
                        broadcastAddresses.addAll(interfaceBroadcasts);
                    } else {
                        log("接口 " + ni.getDisplayName() + " 没有找到广播地址");
                    }
                } catch (SocketException e) {
                    hasPermissionIssue = true;
                    log("访问网络接口 " + ni.getDisplayName() + " 时出现权限问题: " + e.getMessage());
                }
            }

            // 处理特殊情况
            if (broadcastAddresses.isEmpty()) {
                if (!hasAnyValidInterface) {
                    if (hasPermissionIssue) {
                        throw new NetworkInterfaceException("无法访问网络接口，可能是权限问题");
                    } else {
                        log("未找到有效的网络接口，使用默认广播地址");
                    }
                } else {
                    log("未找到广播地址，使用默认广播地址");
                }
                broadcastAddresses.add(FALLBACK_BROADCAST);
                log("使用默认广播地址: " + FALLBACK_BROADCAST);
            }

            log("最终使用的广播地址: " + broadcastAddresses);
            return broadcastAddresses;

        } catch (SocketException e) {
            throw new NetworkInterfaceException("获取网络接口列表失败", e);
        }
    }

    /**
     * 检查网络接口是否有效
     * @param ni 网络接口
     * @return 如果接口有效返回true
     * @throws SocketException 如果检查接口状态时出错
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        if (ni == null) return false;
        
        // 检查基本条件
        if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
            return false;
        }

        // 检查是否是点对点接口
        if (ni.isPointToPoint()) {
            log("跳过点对点接口: " + ni.getDisplayName());
            return false;
        }

        // 检查是否支持广播
        if (!ni.supportsMulticast()) {
            log("跳过不支持广播的接口: " + ni.getDisplayName());
            return false;
        }

        // 检查是否有IPv4地址
        boolean hasIPv4 = ni.getInterfaceAddresses().stream()
                .anyMatch(addr -> addr.getAddress() instanceof Inet4Address);
        if (!hasIPv4) {
            log("跳过没有IPv4地址的接口: " + ni.getDisplayName());
            return false;
        }

        return true;
    }

    /**
     * 获取指定网络接口的广播地址（私有方法）
     * @param networkInterface 网络接口对象
     * @return 该接口的广播地址集合
     */
    private static Set<String> getBroadcastAddresses(NetworkInterface networkInterface) {
        return networkInterface.getInterfaceAddresses().stream()
                .map(InterfaceAddress::getBroadcast)
                .filter(Objects::nonNull)
                .filter(addr -> addr instanceof Inet4Address) // 只保留IPv4地址
                .map(InetAddress::getHostAddress)
                .collect(Collectors.toSet());
    }
}