package com.samsa.core;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Pipe {
    private final UUID id;
    private final BlockingQueue<Message> queue;
    private static final int DEFAULT_CAPACITY = 1;

    public Pipe() {
        this(DEFAULT_CAPACITY);
    }

    public Pipe(int capacity) {
        this.id = UUID.randomUUID();
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public synchronized boolean offer(Message message) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        while (isFull()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        notifyAll();
        return queue.offer(message);
    }

    public synchronized Message poll() {
        while (isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        notifyAll();
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFull() {
        return queue.remainingCapacity() == 0;
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Pipe[id=%s, size=%d, capacity=%d]", id, queue.size(),
                queue.size() + queue.remainingCapacity());
    }
}
