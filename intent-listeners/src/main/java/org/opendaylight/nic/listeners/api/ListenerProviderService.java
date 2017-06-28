package org.opendaylight.nic.listeners.api;

/**
 * Intent Listener service
 */
public interface ListenerProviderService extends AutoCloseable {

    /**
     * Start Intent Listener services
     */
    void start();

    /**
     * Stop Intent Listener services
     */
    void stop();
}
