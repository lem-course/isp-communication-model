package fri.isp;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Environment {

    private static final String NO_LINK = "There is no connection between '%s' and '%s'.";
    private static final String INVALID_NAMES = "Names must not be null or empty";
    private static final String LINK_EXISTS = "There is already a link between '%s' and '%s'";
    private static final String DUPLICATE_NAME = "Agent with name '%s' is already defined; all agents must be unique";
    private static final String NON_EXISTING_AGENT = "Agent '%s' does not exist";
    private static final String MITM_ERROR_DIRECT = "MITM error: Agents '%s' and '%s' are already connected";
    private static final String MITM_ERROR_MITM_EXISTS = "MITM error: There is an existing link between '%s', '%s' and '%s'";

    private final List<Agent> agents = new ArrayList<>();

    private final ConcurrentHashMap<Pair<String, String>, BlockingQueue<byte[]>> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair<String, String>, Pair<String, String>> sendMITM = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair<String, String>, Pair<String, String>> receiveMITM = new ConcurrentHashMap<>();

    /**
     * Creates and registers a new agent with given name.
     *
     * @param agent The name of the agent
     */
    public void add(final Agent agent) {
        for (Agent a : agents) {
            if (a.getName().equals(agent.getName())) {
                throw new IllegalArgumentException(String.format(DUPLICATE_NAME, a.getName()));
            }
        }

        agent.setEnvironment(this);
        agents.add(agent);
    }

    /**
     * Starts the environment
     */
    public void start() {
        for (Agent a : agents) {
            a.start();
        }

        for (Agent a : agents) {
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
    protected void send(final String sender, final String receiver, final byte[] message) {
        try {
            final Pair<String, String> direct = new Pair<>(sender, receiver);
            final Pair<String, String> routed = sendMITM.get(direct);

            final BlockingQueue<byte[]> queue;
            if (routed == null) {
                queue = queues.get(direct);
            } else { // MITM
                queue = queues.get(routed);
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
    protected byte[] receive(final String sender, final String receiver) {
        try {
            final Pair<String, String> direct = new Pair<>(sender, receiver);
            final Pair<String, String> routed = receiveMITM.get(direct);

            final BlockingQueue<byte[]> queue;
            if (routed == null) {
                queue = queues.get(direct);
            } else { // MITM
                queue = queues.get(routed);
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
    public void connect(final String agent1, final String agent2) {
        if (agent1 == null || agent2 == null || agent1.trim().isEmpty() || agent2.trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_NAMES);
        }

        final boolean exists1 = agents.stream().map(Thread::getName).filter(s -> s.equals(agent1)).count() == 1;
        final boolean exists2 = agents.stream().map(Thread::getName).filter(s -> s.equals(agent2)).count() == 1;

        if (!exists1) {
            throw new IllegalArgumentException(String.format(NON_EXISTING_AGENT, agent1));
        }

        if (!exists2) {
            throw new IllegalArgumentException(String.format(NON_EXISTING_AGENT, agent2));
        }

        final Pair<String, String> a2b = new Pair<>(agent1, agent2);
        final Pair<String, String> b2a = new Pair<>(agent2, agent1);

        if (queues.containsKey(a2b) || queues.containsKey(b2a)) {
            throw new IllegalArgumentException(String.format(LINK_EXISTS, agent1, agent2));
        }

        queues.put(a2b, new LinkedBlockingQueue<>());
        queues.put(b2a, new LinkedBlockingQueue<>());
    }

    /**
     * Creates a connection between two agents in the presence of an active attacker: man-in-the-middle.
     * The attacker is able to intercept and modify all messages that are exchanged between agents.
     *
     * @param agentA the first agent
     * @param agentB the second agent
     * @param mitm   man in the middle agent
     */
    public void mitm(String agentA, String agentB, String mitm) {
        if (agentA == null || agentB == null || mitm == null ||
                agentA.trim().isEmpty() || agentB.trim().isEmpty() || mitm.trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_NAMES);
        }

        final Pair<String, String> a2b = new Pair<>(agentA, agentB);
        final Pair<String, String> b2a = new Pair<>(agentB, agentA);
        final Pair<String, String> a2m = new Pair<>(agentA, mitm);
        final Pair<String, String> m2a = new Pair<>(mitm, agentA);
        final Pair<String, String> b2m = new Pair<>(agentB, mitm);
        final Pair<String, String> m2b = new Pair<>(mitm, agentB);

        if (queues.containsKey(a2b) || queues.containsKey(b2a)) {
            throw new IllegalArgumentException(String.format(MITM_ERROR_DIRECT, agentA, agentB));
        }

        sendMITM.put(a2b, a2m);
        sendMITM.put(b2a, b2m);
        receiveMITM.put(a2b, m2b);
        receiveMITM.put(b2a, m2a);

        if (queues.containsKey(a2m) || queues.containsKey(b2m) ||
                queues.containsKey(m2a) || queues.containsKey(m2b)) {
            throw new IllegalArgumentException(String.format(MITM_ERROR_MITM_EXISTS, agentA, mitm, agentB));
        }

        queues.put(a2m, new LinkedBlockingQueue<>());
        queues.put(b2m, new LinkedBlockingQueue<>());
        queues.put(m2a, new LinkedBlockingQueue<>());
        queues.put(m2b, new LinkedBlockingQueue<>());
    }
}
