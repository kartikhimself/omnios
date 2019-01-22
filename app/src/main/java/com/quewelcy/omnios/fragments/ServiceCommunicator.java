package com.quewelcy.omnios.fragments;

import com.quewelcy.omnios.data.Playable;

import java.util.Collection;

public interface ServiceCommunicator {

    void play(Playable playable);

    void seek(int progress);

    void stop();

    void seekLeft(int sec);

    void seekRight(int sec);

    void addToQueue(String path);

    void removeFromQueue(String path);

    Collection<Playable> getQueueCopy();
}
