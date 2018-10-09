package fri.isp;

public abstract class Agent extends Thread {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private Environment env;

    public Agent(final String name) {
        super(name);
    }

    protected void setEnvironment(final Environment env) {
        this.env = env;
    }

    /**
     * Sends a message to given agent
     *
     * @param recipient the identity of the recipient
     * @param data      the message contents
     */
    public void send(final String recipient, final byte[] data) {
        env.send(getName(), recipient, data);
    }

    /**
     * Receives a message from given sender. This is a blocking call: this method will block your program if there
     * is no message.
     *
     * @param sender The identity of the agent that sent the message
     * @return message contents
     */
    public byte[] receive(final String sender) {
        return env.receive(sender, getName());
    }

    @Override
    public final void run() {
        try {
            task();
        } catch (Exception e) {
            synchronized (System.err) {
                e.printStackTrace(System.err);
            }
        }
    }

    public abstract void task() throws Exception;

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
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
