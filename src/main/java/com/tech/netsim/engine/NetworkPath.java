package com.tech.netsim.engine;

import java.util.LinkedList;

/**
 * Represents the path followed by a packet in the network.
 * Since this is a network path, it can be represented as an extended LinkedList of ids.
 */
public class NetworkPath extends LinkedList<String> {

    /**
     * Time it took for a packet to travel the path. Zero, by default, represents an empty path.
     */
    private int time = 0;

    /**
     * Get the time it took the packet to travel the corresponding path
     */
    public int getTime() {
        return time;
    }

    /**
     * Update the time it take the pack to travel though the path.
     *
     * @param time The time (ms)
     */
    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return super.toString().replace(" ", "");
    }
}
