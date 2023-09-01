package dev.francescodema.sunmi_task_printer;


import android.annotation.SuppressLint;
import android.os.RemoteException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Task provider class.
 */
public class TaskProvider {

    /**
     * The constant executor.
     */
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Run function with exception optional.
     *
     * @param <Res>    the type parameter
     * @param function the function
     * @return the optional
     * @throws RemoteException the printer exception
     */
    @SuppressLint("NewApi")
    public static <Res> Optional<Res> runFunctionWithException(Callable<Res> function) throws RemoteException {
        try {

            return Optional.ofNullable(CompletableFuture.supplyAsync(() -> {
                try {
                    return executor.submit(function).get();
                } catch (ExecutionException | InterruptedException ex) {
                    try {
                        throw Objects.requireNonNull(ex.getCause());
                    } catch (RemoteException exception) {
                        throw new CompletionException(exception);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                return null;
            }).join());

        } catch (CompletionException ex) {
            try {
                throw Objects.requireNonNull(ex.getCause());
            } catch (RemoteException possible) {
                throw possible;
            } catch (Throwable ignored) {
            }
        }
        return Optional.empty();
    }

    /**
     * Run function with exception.
     *
     * @param function the function
     * @throws RemoteException the printer exception
     */
    @SuppressLint("NewApi")
    public static void runFunctionWithException(ThrowingRunnable function) throws RemoteException {
        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return executor.submit(function).get();
                } catch (ExecutionException | InterruptedException ex) {
                    try {
                        throw Objects.requireNonNull(ex.getCause());
                    } catch (RemoteException exception) {
                        throw new CompletionException(exception);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                return null;
            }).join();
        } catch (CompletionException ex) {
            try {
                throw Objects.requireNonNull(ex.getCause());
            } catch (RemoteException possible) {
                throw possible;
            } catch (Throwable ignored) {
            }
        }
    }


}
