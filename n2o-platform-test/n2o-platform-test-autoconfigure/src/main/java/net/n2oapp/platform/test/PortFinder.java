package net.n2oapp.platform.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PortFinder {

    private static final Logger logger = LoggerFactory.getLogger(PortFinder.class);

    private static final int MIN_PORT_NUMBER = 49152;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int ATTEMPTS_LIMIT = 10;

    private static List<Integer> usedList = new CopyOnWriteArrayList<>();

    private PortFinder() {
    }

    public static int getPort(String applicationName) {
        int unusedPort = getUnusedPort(getRandomPort());
        logger.info("Find unused port {} for application {}", unusedPort, applicationName);
        return unusedPort;
    }

    private static int getUnusedPort(int startPort) {
        int attemptsCount = 1;
        while (usedList.contains(startPort) || !available(startPort)) {
            if (attemptsCount > ATTEMPTS_LIMIT)
                throw new RuntimeException("Attempts limit exceeded");
            startPort++;// = getRandomPort();
            attemptsCount++;
        }
        usedList.add(startPort);
        return startPort;
    }

    private static int getRandomPort() {
        Random r = new Random();
        return r.nextInt((MAX_PORT_NUMBER - MIN_PORT_NUMBER) + 1) + MIN_PORT_NUMBER;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability, port must be in valid range
     */
    private static boolean available(int port) {

        try (ServerSocket ss = new ServerSocket(port); DatagramSocket ds = new DatagramSocket(port)) {
            ss.setReuseAddress(true);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            logger.info("Сan not use port: {}, error: {}", port, e.getMessage());
        }

        return false;
    }

}
