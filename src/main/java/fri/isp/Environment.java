package fri.isp;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public enum Environment {
    INSTANCE;

    private final List<Agent> agents = new ArrayList<>();
    private final ConcurrentHashMap<Pair<String, String>, BlockingQueue<byte[]>> queues = new ConcurrentHashMap<>();

    public static void add(final Agent agent) {
        INSTANCE.agents.add(agent);
    }

    public static void start() {
        for (Agent a : INSTANCE.agents) {
            a.start();
        }

        for (Agent a : INSTANCE.agents) {
            try {
                a.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void send(final String from, final String to, final byte[] data) {
        try {
            INSTANCE.queues.get(new Pair<>(from, to)).put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] recv(final String sender, final String receiver) {
        try {
            return INSTANCE.queues.get(new Pair<>(sender, receiver)).take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void connect(final String a, final String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Names must not be null");
        }

        final Pair<String, String> a2b = new Pair<>(a, b);
        final Pair<String, String> b2a = new Pair<>(b, a);

        if (INSTANCE.queues.containsKey(a2b) || INSTANCE.queues.containsKey(b2a)) {
            throw new IllegalArgumentException("Agents already connected");
        }

        INSTANCE.queues.put(a2b, new LinkedBlockingQueue<>());
        INSTANCE.queues.put(b2a, new LinkedBlockingQueue<>());
    }
}
