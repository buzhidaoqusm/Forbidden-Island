package com.island.network;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class BroadcastAddressCalculator {
    // 私有构造函数防止实例化
    private BroadcastAddressCalculator() {
        throw new AssertionError("工具类禁止实例化");
    }

    /**
     * 获取所有有效的 IPv4 广播地址
     * @return 广播地址字符串集合（如 ["192.168.1.255", "10.0.255.255"]）
     */
    public static Set<String> getBroadcastAddresses() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(ni -> {
                        try {
                            return ni.isUp() && !ni.isLoopback();
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .flatMap(ni -> getBroadcastAddresses(ni).stream())
                    .collect(Collectors.toSet());
        } catch (SocketException e) {
            System.err.println("获取网络接口失败: " + e.getMessage());
            return Collections.emptySet();
        }
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
                .map(InetAddress::getHostAddress)
                .collect(Collectors.toSet());
    }

    //-------------------------
    // 使用示例
    //-------------------------
    public static void main(String[] args) {
        System.out.println("可用广播地址:");
        getBroadcastAddresses().forEach(System.out::println);
    }
}