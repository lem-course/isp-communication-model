package fri.isp;

public class App {
    public static void main(String[] args) throws InterruptedException {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
                send("bob", "from Alice".getBytes());
                send("charlie", "from Alice".getBytes());
                print("Got %s", new String(recv("bob")));
                print("Got %s", new String(recv("charlie")));
            }
        });

        Environment.add(new Agent("bob") {
            @Override
            public void run() {
                send("alice", "from Bob".getBytes());
                send("charlie", "from Bob".getBytes());
                print("Got %s", new String(recv("alice")));
                print("Got %s", new String(recv("charlie")));
            }
        });

        Environment.add(new Agent("charlie") {
            @Override
            public void run() {
                send("bob", "from Charlie".getBytes());
                send("alice", "from Charlie".getBytes());
                print("Got %s", new String(recv("alice")));
                print("Got %s", new String(recv("bob")));
            }
        });

        Environment.connect("alice", "bob");
        Environment.connect("alice", "charlie");
        Environment.connect("charlie", "bob");
        Environment.start();
    }
}