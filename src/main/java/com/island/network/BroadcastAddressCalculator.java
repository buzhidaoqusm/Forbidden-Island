package com.island.network;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class BroadcastAddressCalculator {
    private BroadcastAddressCalculator() {
        throw new AssertionError("Tool class prohibits instantiation");
    }

    /**
     * Get all valid IPv4 broadcast addresses
     * @ return Broadcast address string collection (such as ["192.168.1.255", "192.168.255.255"])
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
            System.err.println("Failed to obtain network interface: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Get the broadcast address of the specified network interface (private method)
     * @ param networkInterface Network Interface Object
     * @ return The set of broadcast addresses for this interface
     */
    private static Set<String> getBroadcastAddresses(NetworkInterface networkInterface) {
        return networkInterface.getInterfaceAddresses().stream()
                .map(InterfaceAddress::getBroadcast)
                .filter(Objects::nonNull)
                .map(InetAddress::getHostAddress)
                .collect(Collectors.toSet());
    }

}