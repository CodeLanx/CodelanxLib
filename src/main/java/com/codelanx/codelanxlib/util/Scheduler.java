/*
 * Copyright (C) 2015 Codelanx, All Rights Reserved
 *
 * This work is licensed under a Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 *
 * This program is protected software: You are free to distrubute your
 * own use of this software under the terms of the Creative Commons BY-NC-ND
 * license as published by Creative Commons in the year 2015 or as published
 * by a later date. You may not provide the source files or provide a means
 * of running the software outside of those licensed to use it.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the Creative Commons BY-NC-ND license
 * long with this program. If not, see <https://creativecommons.org/licenses/>.
 */
package com.codelanx.codelanxlib.util;

import com.codelanx.codelanxlib.CodelanxLib;
import com.codelanx.codelanxlib.logging.Debugger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

/**
 * Façade utility class for simplifying scheduling tasks
 *
 * @since 0.1.0
 * @author 1Rogue
 * @version 0.1.0
 */
public final class Scheduler {

    private static final List<ScheduledFuture<?>> executives = new ArrayList<>(); //TODO implement a cache pattern
    private static ScheduledExecutorService es;

    private Scheduler() {
    }

    /**
     * Runs a repeating asynchronous task
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param r The runnable to execute
     * @param startAfter Time (in seconds) to wait before execution
     * @param delay Time (in seconds) between execution to wait
     * @return The scheduled Task
     */
    public static ScheduledFuture<?> runAsyncTaskRepeat(Runnable r, long startAfter, long delay) {
        ScheduledFuture<?> sch = Scheduler.getService().scheduleWithFixedDelay(r, startAfter, delay, TimeUnit.SECONDS);
        Scheduler.executives.add(sch);
        return sch;
    }

    /**
     * Runs a single asynchronous task
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param r The runnable to execute
     * @param delay Time (in seconds) to wait before execution
     * @return The scheduled Task
     */
    public static ScheduledFuture<?> runAsyncTask(Runnable r, long delay) {
        ScheduledFuture<?> sch = Scheduler.getService().schedule(r, delay, TimeUnit.SECONDS);
        Scheduler.executives.add(sch);
        return sch;
    }

    /**
     * Immediately runs a single asynchronous task
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param r The runnable to execute
     * @return The scheduled Task
     */
    public static ScheduledFuture<?> runAsyncTask(Runnable r) {
        return Scheduler.runAsyncTask(r, 0);
    }

    /**
     * Runs a task after a specified delay on Bukkit's main thread
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param r The {@link Runnable} to execute
     * @param delay Time (in seconds) to wait before execution
     * @return The scheduled task that will execute the provided runnable
     */
    public static ScheduledFuture<?> runSyncTask(Runnable r, long delay) {
        //TODO: hook bukkit's scheduler directly for this operation
        return Scheduler.runAsyncTask(() -> {
            Bukkit.getServer().getScheduler().callSyncMethod(CodelanxLib.get(), () -> {
                r.run();
                return null;
            });
        }, delay);
    }

    /**
     * Runs a task after a specified time on Bukkit's main thread, and repeats
     * it in intervals as specified by the {@code delay} parameter
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param r The {@link Runnable} to execute
     * @param startAfter Time (in seconds) to wait before executing at all
     * @param delay Time (in seconds) to wait in between executions
     * @return The scheduled task that will execute the provided runnable
     */
    public static ScheduledFuture<?> runSyncTaskRepeat(Runnable r, long startAfter, long delay) {
        return Scheduler.runAsyncTaskRepeat(() -> {
            Bukkit.getServer().getScheduler().callSyncMethod(CodelanxLib.get(), () -> {
                r.run();
                return null;
            });
        }, startAfter, delay);
    }
    
    /**
     * Runs a Callable
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param <T> The return type of the {@link Callable}
     * @param c The callable to execute
     * @param delay Time (in seconds) to wait before execution
     * @return The scheduled Task
     */
    public static <T> ScheduledFuture<T> runCallable(Callable<T> c, long delay) {
        ScheduledFuture<T> sch = Scheduler.getService().schedule(c, delay, TimeUnit.SECONDS);
        Scheduler.executives.add(sch);
        return sch;
    }
    
    /**
     * Cancels all running tasks/threads and clears the cached queue.
     * 
     * @since 0.1.0
     * @version 0.1.0
     */
    public static void cancelAllTasks() {
        Scheduler.executives.forEach(s -> s.cancel(false));
        Scheduler.executives.clear();
        try {
            Scheduler.getService().awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Debugger.error(ex, "Error halting scheduler service");
        }
    }

    /**
     * Returns the underlying {@link ScheduledExecutorService} used for this
     * utility class
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @return The underlying {@link ScheduledExecutorService}
     */
    public static ScheduledExecutorService getService() {
        if (Scheduler.es == null || Scheduler.es.isShutdown()) {
            Scheduler.es = Executors.newScheduledThreadPool(10); //Going to find an expanding solution to this soon
        }
        return Scheduler.es;
    }

}
