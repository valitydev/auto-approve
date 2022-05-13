package dev.vality.auto.approve.handler;

public interface EventHandler<T> {
    boolean isAccept(T event);

    void handle(T event);
}
