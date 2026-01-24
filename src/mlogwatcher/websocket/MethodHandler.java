package mlogwatcher.websocket;

public interface MethodHandler {
    Response handle(Request request);
}
