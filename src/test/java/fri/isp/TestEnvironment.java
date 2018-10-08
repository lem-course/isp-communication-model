package fri.isp;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestEnvironment {
    @Test
    public void sendAndReceive() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);

        final byte[] sent = "test-data".getBytes();

        Environment.add(new Agent("alice1") {
            @Override
            public void run() {
                send("bob1", sent);
                latch.countDown();
            }
        });

        Environment.add(new Agent("bob1") {
            @Override
            public void run() {
                final byte[] received = receive("alice1");
                Assert.assertArrayEquals(received, sent);
                latch.countDown();
            }
        });

        Environment.connect("alice1", "bob1");
        Environment.start();
        latch.await();
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectWrongName() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {

            }
        });

        Environment.connect("alice", "missing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName1() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });

        Environment.connect("alice", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName2() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });

        Environment.connect("", "alice");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName3() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });

        Environment.connect("alice", " ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectInvalidName4() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });

        Environment.connect(" ", "alice");
    }

    @Test(expected = IllegalArgumentException.class)
    public void connectExisting() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });
        Environment.add(new Agent("bob") {
            @Override
            public void run() {
            }
        });

        Environment.connect("bob", "alice");
        Environment.connect("alice", "bob");
    }

    @Test
    public void mitmSimple() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final byte[] data = new byte[]{1, 2, 3, 4, 5};

        Environment.add(new Agent("a") {
            @Override
            public void run() {
                send("b", data);
                latch.countDown();
            }
        });
        Environment.add(new Agent("m") {
            @Override
            public void run() {
                final byte[] intercepted = receive("a");
                Assert.assertArrayEquals(data, intercepted);
                send("b", intercepted);
                latch.countDown();
            }
        });
        Environment.add(new Agent("b") {
            @Override
            public void run() {
                final byte[] received = receive("a");
                Assert.assertArrayEquals(data, received);
                latch.countDown();
            }
        });

        Environment.mitm("a", "b", "m");
        Environment.start();
        latch.await();
    }

    @Test(expected = IllegalArgumentException.class)
    public void mitmExistingDirect() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });
        Environment.add(new Agent("bob") {
            @Override
            public void run() {
            }
        });

        Environment.connect("alice", "bob");
        Environment.mitm("alice", "bob", "ccc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mitmExistingToMITM() {
        Environment.add(new Agent("alice") {
            @Override
            public void run() {
            }
        });
        Environment.add(new Agent("bob") {
            @Override
            public void run() {
            }
        });
        Environment.add(new Agent("mitm") {
            @Override
            public void run() {
            }
        });

        Environment.connect("alice", "mitm");
        Environment.mitm("alice", "alice", "mitm");
    }


}
