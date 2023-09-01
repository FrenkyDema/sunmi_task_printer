package dev.francescodema.sunmi_task_printer;

/**
 * The interface Throwing runnable.
 */
@FunctionalInterface
public interface ThrowingRunnable extends Runnable {
    private static <E extends Exception> void throwUnchecked(Throwable t) throws E {
        throw (E) t;
    }

    @Override
    default void run() {
        try {
            tryRun();
        } catch (final Throwable t) {
            throwUnchecked(t);
        }
    }

    /**
     * Try run.
     *
     * @throws Throwable the throwable
     */
    void tryRun() throws Throwable;
}