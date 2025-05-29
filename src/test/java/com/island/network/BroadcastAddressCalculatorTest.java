package com.island.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;

/**
 * Test class for BroadcastAddressCalculator functionality
 */
class BroadcastAddressCalculatorTest {

    /**
     * Tests broadcast address calculation with a class C network
     */
    @Test
    void testCalculateBroadcastAddressClassC() throws Exception {
        InetAddress ipAddress = InetAddress.getByName("192.168.1.100");
        InetAddress subnetMask = InetAddress.getByName("255.255.255.0");
        
        InetAddress broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        
        assertEquals("192.168.1.255", broadcastAddress.getHostAddress());
    }

    /**
     * Tests broadcast address calculation with a class B network
     */
    @Test
    void testCalculateBroadcastAddressClassB() throws Exception {
        InetAddress ipAddress = InetAddress.getByName("172.16.50.100");
        InetAddress subnetMask = InetAddress.getByName("255.255.0.0");
        
        InetAddress broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        
        assertEquals("172.16.255.255", broadcastAddress.getHostAddress());
    }

    /**
     * Tests broadcast address calculation with a custom subnet mask
     */
    @Test
    void testCalculateBroadcastAddressCustomSubnet() throws Exception {
        InetAddress ipAddress = InetAddress.getByName("192.168.100.100");
        InetAddress subnetMask = InetAddress.getByName("255.255.252.0");
        
        InetAddress broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        
        assertEquals("192.168.103.255", broadcastAddress.getHostAddress());
    }

    /**
     * Tests local IP and subnet detection functionality
     * Note: This test may be environment-dependent
     */
    @Test
    void testGetLocalIpAndSubnet() throws Exception {
        String broadcastAddress = BroadcastAddressCalculator.getLocalIpAndSubnet();
        
        // The result might be null if no suitable interface is found
        if (broadcastAddress != null) {
            // Verify the broadcast address format
            assertTrue(isValidIpv4Address(broadcastAddress), 
                    "Broadcast address should be a valid IPv4 address");
            
            // Verify it ends with .255 (most common for IPv4 networks)
            assertTrue(broadcastAddress.endsWith(".255") || broadcastAddress.contains(".255."),
                    "Broadcast address should typically end with .255 or contain .255.");
        }
    }

    /**
     * Helper method to validate IPv4 address format
     */
    private boolean isValidIpv4Address(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Tests broadcast address calculation with edge case IP addresses
     */
    @Test
    void testCalculateBroadcastAddressEdgeCases() throws Exception {
        // Test with network address
        InetAddress ipAddress = InetAddress.getByName("192.168.0.0");
        InetAddress subnetMask = InetAddress.getByName("255.255.255.0");
        InetAddress broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        assertEquals("192.168.0.255", broadcastAddress.getHostAddress());

        // Test with last usable IP in network
        ipAddress = InetAddress.getByName("192.168.0.254");
        broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        assertEquals("192.168.0.255", broadcastAddress.getHostAddress());
    }

    /**
     * Tests broadcast address calculation with different subnet masks
     */
    @Test
    void testCalculateBroadcastAddressVariousSubnets() throws Exception {
        InetAddress ipAddress = InetAddress.getByName("192.168.1.100");
        
        // Test with /24 subnet
        InetAddress subnetMask = InetAddress.getByName("255.255.255.0");
        InetAddress broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        assertEquals("192.168.1.255", broadcastAddress.getHostAddress());
        
        // Test with /16 subnet
        subnetMask = InetAddress.getByName("255.255.0.0");
        broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        assertEquals("192.168.255.255", broadcastAddress.getHostAddress());
        
        // Test with /20 subnet
        subnetMask = InetAddress.getByName("255.255.240.0");
        broadcastAddress = BroadcastAddressCalculator.calculateBroadcastAddress(ipAddress, subnetMask);
        assertEquals("192.168.15.255", broadcastAddress.getHostAddress());
    }
} 