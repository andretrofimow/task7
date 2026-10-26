import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements Executor {
    private final LinkedList<Runnable> taskList = new LinkedList<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger taskCounter = new AtomicInteger(0); // ?
    private final Thread[] threads;


    public CustomThreadPool(int threadsCount) {
        /* create and add threads to list */
        threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new CustomWorker());
            threads[i].start();
        }
    }

    @Override
    public void execute(@SuppressWarnings("all") Runnable command) {
        /* check instead of @NotNull on command */
        if (command == null)
            throw new NullPointerException("Command is empty");

        /* when pool is not shutdown, add task in the end of list */
        if (!isShutdown.get()) {
            synchronized (taskList) {
                taskList.offer(command);
                taskCounter.incrementAndGet();
            }
        }
        else
            throw new IllegalStateException("Command " + command.getClass().getName() + " can't be execute");
    }

    public void shutdown() {
        isShutdown.set(true);

        /* new thread for interrupting thread pool and itself */
        new Thread(() -> {
            try {
                while (true) {
                    if (taskCounter.get() == 0) {
                        for (Thread thread : threads) {
                            thread.interrupt();
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void awaitTermination() throws InterruptedException {
        /* when we have shutdown flag - join all threads */
        if (isShutdown.get()) {
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }

    /* custom worker (in private class) for custom thread poll */
    private class CustomWorker implements Runnable {
        @Override
        public void run() {
            /* when thread is ready for run, get first task and run it */
            while (taskCounter.get() != 0 || !isShutdown.get()) {
                Runnable task;
                synchronized (taskList) {
                    task = taskList.poll();
                }
                if (task != null) {
                    task.run();
                    taskCounter.decrementAndGet();
                }
            }
        }
    }
}
