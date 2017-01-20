package fri.isp;

import javax.xml.bind.DatatypeConverter;

public abstract class Agent extends Thread {
    public final String name;

    public Agent(final String name) {
        super(name);
        this.name = name;
    }

    public void send(final String to, byte[] data) {
        Environment.send(name, to, data);
    }

    public byte[] recv(final String from) {
        return Environment.recv(from, name);
    }

    @Override
    public abstract void run();

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
