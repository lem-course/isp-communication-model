package fri.isp;

public class AppMITM {
    public static void main(String[] args) {
        final Environment env = new Environment();
        env.add(new Agent("alice") {
            @Override
            public void task() {
                send("bob", "from Alice".getBytes());
                print("Got '%s'", new String(receive("bob")));
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() {
                send("alice", "from Bob".getBytes());
                print("Got '%s'", new String(receive("alice")));
            }
        });

        env.add(new Agent("mallory") {
            @Override
            public void task() {
                final byte[] fromAlice = receive("alice");
                print("Forwarding '%s' from '%s' to '%s'", new String(fromAlice), "alice", "bob");
                send("bob", new String(fromAlice).toUpperCase().getBytes());

                final byte[] fromBob = receive("bob");
                print("Forwarding '%s' from '%s' to '%s'", new String(fromBob), "bob", "alice");
                send("alice", new String(fromBob).toUpperCase().getBytes());
                print("Done");
            }
        });

        env.mitm("alice", "bob", "mallory");
        env.start();
    }
}
