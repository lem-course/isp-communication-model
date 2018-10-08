package fri.isp;

public class AppMITM {
    public static void main(String[] args) {

        Environment.add(new Agent("alice") {
            @Override
            public void run() {
                send("bob", "from Alice".getBytes());
                print("Got '%s'", new String(receive("bob")));
            }
        });

        Environment.add(new Agent("bob") {
            @Override
            public void run() {
                send("alice", "from Bob".getBytes());
                print("Got '%s'", new String(receive("alice")));
            }
        });

        Environment.add(new Agent("mallory") {
            @Override
            public void run() {
                final byte[] fromAlice = receive("alice");
                print("Forwarding '%s' from '%s' to '%s'", new String(fromAlice), "alice", "bob");
                send("bob", new String(fromAlice).toUpperCase().getBytes());

                final byte[] fromBob = receive("bob");
                print("Forwarding '%s' from '%s' to '%s'", new String(fromBob), "bob", "alice");
                send("alice", new String(fromBob).toUpperCase().getBytes());
                print("Done");
            }
        });

        Environment.mitm("alice", "bob", "mallory");
        Environment.start();
    }
}
