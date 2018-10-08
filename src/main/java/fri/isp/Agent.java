package fri.isp;

import javax.xml.bind.DatatypeConverter;

public abstract class Agent extends Thread {
    public Agent(final String name) {
        super(name);
    }

    /**
     * Sends a message to given agent
     *
     * @param recipient the identity of the recipient
     * @param data      the message contents
     */
    public void send(final String recipient, final byte[] data) {
        Environment.send(getName(), recipient, data);
    }

    /**
     * Receives a message from given sender. This is a blocking call: this method will block your program if there
     * is no message.
     *
     * @param sender The identity of the agent that sent the message
     * @return message contents
     */
    public byte[] receive(final String sender) {
        return Environment.receive(sender, getName());
    }

    @Override
    public abstract void run();

    /**
     * A utility method that prints to standard output. The method accepts the usual printf commands.
     *
     * @param string String to be printed
     * @param obj    a variable number of objects of various types
     */
    public void print(String string, Object... obj) {
        synchronized (System.out) {
            System.out.printf("[%s] ", getName());
            System.out.printf(string, obj);
            System.out.println();
        }
    }

    public String hex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
