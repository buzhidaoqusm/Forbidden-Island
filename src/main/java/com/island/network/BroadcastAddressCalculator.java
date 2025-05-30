package com.island.network;

import java.net.*;
import java.util.Enumeration;

/**
 * Utility class for calculating broadcast addresses and managing network interfaces.
 * This class provides functionality to determine the appropriate network interface
 * and calculate broadcast addresses for network communication.
 */
public class BroadcastAddressCalculator {

    /**
     * Calculate the broadcast address using IP address and subnet mask
     * @param ipAddress The IP address to use for calculation
     * @param subnetMask The subnet mask to use for calculation
     * @return The calculated broadcast address
     * @throws Exception If there's an error during address calculation
     */
    public static InetAddress calculateBroadcastAddress(InetAddress ipAddress, InetAddress subnetMask) throws Exception {
        byte[] ipBytes = ipAddress.getAddress();
        byte[] maskBytes = subnetMask.getAddress();

        byte[] broadcastBytes = new byte[ipBytes.length];

        // Calculate broadcast address by performing bitwise operations
        for (int i = 0; i < ipBytes.length; i++) {
            broadcastBytes[i] = (byte) (ipBytes[i] | (~maskBytes[i] & 0xFF));
        }

        return InetAddress.getByAddress(broadcastBytes);
    }

    /**
     * Get the local IPv4 address and subnet mask by selecting an appropriate network interface.
     * This method filters out unsuitable interfaces such as loop-back, disconnected, and virtual interfaces.
     * @return The broadcast address as a string, or null if no suitable interface is found
     * @throws Exception If there's an error accessing network interfaces
     */
    public static String getLocalIpAndSubnet() throws Exception {
        // Get all network interfaces on the local machine
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

        NetworkInterface selectedInterface = null;
        InetAddress selectedInetAddress = null;
        InetAddress selectedSubnetMask = null;

        while (networks.hasMoreElements()) {
            NetworkInterface network = networks.nextElement();

            // Only select active interfaces, exclude loopback and disconnected interfaces
            if (network.isUp() && !network.isLoopback() && !isDisconnected(network)) {

                // Get all IP addresses for this network interface
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();

                    // Only process IPv4 addresses
                    if (inetAddress instanceof Inet4Address) {
                        short netmask = 0;
                        boolean validInterface = false;

                        for (InterfaceAddress address : network.getInterfaceAddresses()) {
                            if (address.getAddress().equals(inetAddress)) {
                                netmask = (short) address.getNetworkPrefixLength();
                                // Exclude interfaces with subnet mask 255.255.255.255
                                if (netmask != 32) {
                                    validInterface = true;
                                }
                                break;
                            }
                        }

                        if (validInterface) {
                            // Construct subnet mask from prefix length
                            int mask = 0xffffffff << (32 - netmask);
                            InetAddress subnetMask = InetAddress.getByAddress(new byte[] {
                                    (byte) ((mask >> 24) & 0xFF),
                                    (byte) ((mask >> 16) & 0xFF),
                                    (byte) ((mask >> 8) & 0xFF),
                                    (byte) (mask & 0xFF)
                            });

                            // Ensure we select a valid interface with correct IP address
                            if (selectedInterface == null || !network.getName().contains("vmnet")) {  // Exclude virtual network adapters
                                selectedInterface = network;
                                selectedInetAddress = inetAddress;
                                selectedSubnetMask = subnetMask;
                            }
                        }
                    }
                }
            }
        }

        // Ensure a valid interface was selected
        if (selectedInterface != null && selectedInetAddress != null && selectedSubnetMask != null) {
            // Calculate the broadcast address
            InetAddress broadcastAddress = calculateBroadcastAddress(selectedInetAddress, selectedSubnetMask);
            return broadcastAddress.getHostAddress();
        } else {
            System.out.println("No suitable network interface found.");
            return null;
        }
    }

    /**
     * Check if a network interface is in disconnected state
     * @param network The network interface to check
     * @return true if the interface is disconnected, false otherwise
     * @throws SocketException If there's an error accessing the network interface
     */
    private static boolean isDisconnected(NetworkInterface network) throws SocketException {
        return !network.isUp();
    }
}
