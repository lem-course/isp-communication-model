package fri.isp;

public class App {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void run() {
                send("bob", "from Alice".getBytes());
                send("charlie", "from Alice".getBytes());
                print("Got '%s'", new String(receive("bob")));
                print("Got '%s'", new String(receive("charlie")));
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void run() {
                send("alice", "from Bob".getBytes());
                send("charlie", "from Bob".getBytes());
                print("Got '%s'", new String(receive("alice")));
                print("Got '%s'", new String(receive("charlie")));
            }
        });

        env.add(new Agent("charlie") {
            @Override
            public void run() {
                send("bob", "from Charlie".getBytes());
                send("alice", "from Charlie".getBytes());
                print("Got '%s'", new String(receive("alice")));
                print("Got '%s'", new String(receive("bob")));
            }
        });

        env.connect("alice", "bob");
        env.connect("alice", "charlie");
        env.connect("charlie", "bob");
        env.start();
    }
}
