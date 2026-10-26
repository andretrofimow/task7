import java.util.concurrent.ThreadLocalRandom;

public class ThreadsExercises {
    public static void main(String[] args) {
        /* settings values */
        int tasksCount = 10;
        int threadsNumber = 8;
        long randomValueFrom = 2000;
        long randomValueTo = 7000;

        CustomThreadPool customThreadPool = new CustomThreadPool(threadsNumber);

        for (int i = 0; i < tasksCount; i++) {
            /* more safety use another variable */
            int finalI = i;
            customThreadPool.execute(
                    () -> {
                        long start = System.currentTimeMillis();
                        System.out.println("In thread " + Thread.currentThread().getName()
                                + " task number " + finalI + " was started.");

                        long randomValue = ThreadLocalRandom.current().nextLong(randomValueFrom, randomValueTo);
                        try {
                            Thread.sleep(randomValue);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("In thread " + Thread.currentThread().getName()
                                + " task number " + finalI + " ended in "
                                + (System.currentTimeMillis() - start) / 1000 + " seconds.");
                    }
            );
        }

        customThreadPool.shutdown();
        /* that message will be before end of tasks (because before awaitTermination) */
        System.out.println("I'm working yet");

        /* try to execute task after shutdown */
        try {
            customThreadPool.execute(() -> System.out.println("I try to execute"));
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }

        /* await termination */
        try {
            customThreadPool.awaitTermination();
        } catch (InterruptedException exception) {
            System.out.println(exception.getMessage());
        }

        /* that message will be after end of tasks (because after awaitTermination) */
        System.out.println("I'm done");
    }
}
