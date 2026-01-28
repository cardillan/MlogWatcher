package mlogwatcher.websocket;

import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;

public interface MethodHandler {
    Response handle(Request request);
}
