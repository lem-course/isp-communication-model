package fri.isp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestEnvironment {

    private Environment env;

    @Before
    public void setup() {
        env = new Environment();
    }

    @Test
    public void sendAndReceive() {
        final byte[] sent = "test-data".getBytes();

        env.add(new Agent("alice1") {
            @Override
            public void task() {
                send("bob1", sent);
            }
        });

        env.add(new Agent("bob1") {
            @Override
            public void task() {
                final byte[] received = receive("alice1");
                Assert.assertArrayEquals(received, sent);
            }
        });

        env.connect("alice1", "bob1");
        env.start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectWrongName() {
        env.add(new Agent("alice") {
            @Override
            public void task() {

            }
        });

        env.connect("alice", "missing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName1() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });

        env.connect("alice", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName2() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });

        env.connect("", "alice");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName3() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });

        env.connect("alice", " ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName4() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });

        env.connect(" ", "alice");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectExisting() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });
        env.add(new Agent("bob") {
            @Override
            public void task() {
            }
        });

        env.connect("bob", "alice");
        env.connect("alice", "bob");
    }

    @Test
    public void mitmSimple() {
        final byte[] data = new byte[]{1, 2, 3, 4, 5};

        env.add(new Agent("a") {
            @Override
            public void task() {
                send("b", data);
            }
        });
        env.add(new Agent("m") {
            @Override
            public void task() {
                final byte[] intercepted = receive("a");
                Assert.assertArrayEquals(data, intercepted);
                send("b", intercepted);
            }
        });
        env.add(new Agent("b") {
            @Override
            public void task() {
                final byte[] received = receive("a");
                Assert.assertArrayEquals(data, received);
            }
        });

        env.mitm("a", "b", "m");
        env.start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void mitmExistingDirect() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });
        env.add(new Agent("bob") {
            @Override
            public void task() {
            }
        });

        env.connect("alice", "bob");
        env.mitm("alice", "bob", "ccc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mitmExistingToMITM() {
        env.add(new Agent("alice") {
            @Override
            public void task() {
            }
        });
        env.add(new Agent("bob") {
            @Override
            public void task() {
            }
        });
        env.add(new Agent("mitm") {
            @Override
            public void task() {
            }
        });

        env.connect("alice", "mitm");
        env.mitm("alice", "alice", "mitm");
    }
}
