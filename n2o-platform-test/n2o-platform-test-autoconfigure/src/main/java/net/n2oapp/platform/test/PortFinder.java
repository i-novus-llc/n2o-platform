package net.n2oapp.platform.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PortFinder {

    private static final Logger logger = LoggerFactory.getLogger(PortFinder.class);

    private static final int MIN_PORT_NUMBER = 49152;
    private static final int MAX_PORT_NUMBER = 65535;

    private static List<Integer> usedList = new CopyOnWriteArrayList();

    private PortFinder() {
    }

    public static int getPort() {
        return getUnusedPort(MIN_PORT_NUMBER);
    }

    @SuppressWarnings("all")
    private static int getUnusedPort(int startPort) {
        while (!available(startPort) || usedList.contains(startPort)) {
            startPort++;
        }
        usedList.add(startPort);
        return startPort;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    private static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        try(ServerSocket ss = new ServerSocket(port); DatagramSocket ds = new DatagramSocket(port);) {
            ss.setReuseAddress(true);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            logger.error("cannot search port", e );
        }

        return false;
    }

}
