package fri.isp;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public enum Environment {
    INSTANCE;

    private static final String NO_LINK = "There is no connection between '%s' and '%s'.";
    private static final String INVALID_NAMES = "Names must not be null or empty";
    private static final String LINK_EXISTS = "There is already a link between '%s' and '%s'";
    private static final String DUPLICATE_NAME = "Agent with name '%s' is already defined; all agents must be unique";

    private final List<Agent> agents = new ArrayList<>();

    private final ConcurrentHashMap<Pair<String, String>, BlockingQueue<byte[]>> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair<String, String>, Pair<String, String>> sendMITM = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair<String, String>, Pair<String, String>> receiveMITM = new ConcurrentHashMap<>();

    /**
     * Creates and registers a new agent with given name.
     *
     * @param agent The name of the agent
     */
    public static void add(final Agent agent) {
        for (Agent a : INSTANCE.agents) {
            if (a.getName().equals(agent.getName())) {
                throw new IllegalArgumentException(String.format(DUPLICATE_NAME, a.getName()));
            }
        }

        INSTANCE.agents.add(agent);
    }

    /**
     * Starts the environment
     */
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

    /**
     * Enqueues a message from sender to receiver
     *
     * @param sender   agent sending enqueuing the message
     * @param receiver agent to whom the message is sent
     * @param message  message contents
     */
    protected static void send(final String sender, final String receiver, final byte[] message) {
        try {
            final Pair<String, String> direct = new Pair<>(sender, receiver);
            final Pair<String, String> routed = INSTANCE.sendMITM.get(direct);

            final BlockingQueue<byte[]> queue;
            if (routed == null) {
                queue = INSTANCE.queues.get(direct);
            } else { // MITM
                queue = INSTANCE.queues.get(routed);
            }

            if (queue == null) {
                throw new Error(String.format(NO_LINK, sender, receiver));
            }

            queue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a message sent by sender to receiver. This is a blocking call: this method will
     * wait indefinitely if there is not message.
     *
     * @param sender   agent that sent the message
     * @param receiver agent to whom the message was sent
     * @return the message contents
     */
    protected static byte[] receive(final String sender, final String receiver) {
        try {
            final Pair<String, String> direct = new Pair<>(sender, receiver);
            final Pair<String, String> routed = INSTANCE.receiveMITM.get(direct);

            final BlockingQueue<byte[]> queue;
            if (routed == null) {
                queue = INSTANCE.queues.get(direct);
            } else { // MITM
                queue = INSTANCE.queues.get(routed);
            }

            if (queue == null) {
                throw new Error(String.format(NO_LINK, sender, receiver));
            }

            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a connection between two agents. Once the connection is made,
     * agents can communicate.
     *
     * @param agent1 the first agent
     * @param agent2 the second agent
     */
    public static void connect(final String agent1, final String agent2) {
        if (agent1 == null || agent2 == null || agent1.trim().isEmpty() || agent2.trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_NAMES);
        }

        final Pair<String, String> a2b = new Pair<>(agent1, agent2);
        final Pair<String, String> b2a = new Pair<>(agent2, agent1);

        if (INSTANCE.queues.containsKey(a2b) || INSTANCE.queues.containsKey(b2a)) {
            throw new Error(String.format(LINK_EXISTS, agent1, agent2));
        }

        INSTANCE.queues.put(a2b, new LinkedBlockingQueue<>());
        INSTANCE.queues.put(b2a, new LinkedBlockingQueue<>());
    }

    /**
     * Creates a connection between two agents in the presence of an active attacker: man-in-the-middle.
     * The attacker is able to intercept and modify all messages that are exchanged between agents.
     *
     * @param agent1 the first agent
     * @param agent2 the second agent
     * @param mitm   man in the middle agent
     */
    public static void mitm(String agent1, String agent2, String mitm) {
        if (agent1 == null || agent2 == null || mitm == null || agent1.trim().isEmpty() || agent2.trim().isEmpty() || mitm.trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_NAMES);
        }

        final Pair<String, String> a2b = new Pair<>(agent1, agent2);
        final Pair<String, String> b2a = new Pair<>(agent2, agent1);
        final Pair<String, String> a2m = new Pair<>(agent1, mitm);
        final Pair<String, String> m2a = new Pair<>(mitm, agent1);
        final Pair<String, String> b2m = new Pair<>(agent2, mitm);
        final Pair<String, String> m2b = new Pair<>(mitm, agent2);

        INSTANCE.sendMITM.put(a2b, a2m);
        INSTANCE.sendMITM.put(b2a, b2m);
        INSTANCE.receiveMITM.put(a2b, m2b);
        INSTANCE.receiveMITM.put(b2a, m2a);

        INSTANCE.queues.put(a2m, new LinkedBlockingQueue<>());
        INSTANCE.queues.put(b2m, new LinkedBlockingQueue<>());
        INSTANCE.queues.put(m2a, new LinkedBlockingQueue<>());
        INSTANCE.queues.put(m2b, new LinkedBlockingQueue<>());
    }
}
