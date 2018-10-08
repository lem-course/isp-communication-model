package fri.isp;

public class AppError {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() {
                print("AA");
                print("AA");
                print(1 / 0 + " ");
                print("AA");
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() {
                print("BB");
                print("BB");
                print("BB");
                print("BB");
                print("BB");
                print("BB");
                print("BB");
                print("BB");

            }
        });

        env.start();
    }
}
