package mlogwatcher.websocket;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.Nullable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mlogwatcher.Constants;
import mlogwatcher.ProcessorUpdater;
import mlogwatcher.Settings;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MlogServer extends WebSocketServer {
    @Nullable
    private static MlogServer server;

    private boolean open;

    private final Map<String, MethodHandler> handlers = new HashMap<>();

    MlogServer(int port) {
        super(new InetSocketAddress(port));

        handlers.put(Request.UPDATE_SELECTED_PROCESSOR, new UpdateSelectedProcessorHandler());
        handlers.put(Request.UPDATE_PROCESSORS_ON_MAP, new UpdateProcessorsOnMapHandler());
        handlers.put(Request.PUT_SCHEMATIC_IN_LIBRARY, new PutSchematicInLibraryHandler());
        handlers.put(Request.EXTRACT_SELECTED_PROCESSOR_CODE, new ExtractSelectedProcessorCodeHandler());
        handlers.put(Request.EXTRACT_SELECTED_SCHEMATIC, new ExtractSelectedSchematicHandler());
    }

    public static void startServer() {
        if (server != null) return;
        Settings.updateWebsocketStatus(false);
        server = new MlogServer(Core.settings.getInt(Constants.Settings.websocketPort));
        server.start();
    }

    public static void stopServer() {
        if (server == null) return;

        try {
            server.stop();
        } catch (InterruptedException ignored) {

        }
        server = null;
    }

    public static void restartServer() {
        stopServer();
        startServer();
    }

    public static boolean isOpen() {
        return server != null && !server.open;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String path = conn.getResourceDescriptor();

        if (path.equals("/")) {
            Core.app.post(() -> handleLegacyMessage(conn, message));
        } else if (path.equals("/v1")) {
            Core.app.post(() -> handleMessage(conn, message));
        } else {
            Log.err("[MlogWatcher] unknown websocket path: " + path);
        }
    }

    // Legacy
    public static final String STATUS_OK = "ok";
    public static final String STATUS_NO_PROCESSOR = "no_processor";

    private void handleLegacyMessage(WebSocket conn, String message) {
        if (!Core.settings.getBool(Constants.Settings.legacyApiOff)) {
            boolean processorAttached = ProcessorUpdater.insertLogic(message);
            conn.send(processorAttached ? STATUS_OK : STATUS_NO_PROCESSOR);
        }
    }

    private void handleMessage(WebSocket conn, String message) {
        Request request = decodeRequest(conn, message);
        if (request == null) return;

        Response response;

        MethodHandler handler = handlers.get(request.getMethod());
        if (handler == null) {
            Log.err("[MlogWatcher] unhandled method " + request.getMethod());
            response = Response.error(Response.ERR_UNKNOWN_METHOD);
        } else {
            try {
                response = handler.handle(request);
            } catch (Throwable th) {
                Log.err("[MlogWatcher] websocket request processing error", th);
                response = Response.error(Response.ERR_INTERNAL_ERROR);
            }
        }

        sendResponse(conn, response, request.getInvocationId());
    }

    // Not thread safe
    // All requests are processed on the main thread though
    private final ObjectMapper mapper = new ObjectMapper();

    private Request decodeRequest(WebSocket conn, String message) {
        try {
            return mapper.readValue(message, Request.class);
        } catch (Throwable th) {
            if (th instanceof JsonProcessingException) {
                try {
                    Map<String, Object> map = mapper.readValue(message, new TypeReference<>() {});
                    //noinspection SuspiciousMethodCalls
                    if (map.containsKey("method") && map.containsKey("invocation_id")
                            && !handlers.containsKey(map.get("method"))) {
                        Log.err("[MlogWatcher] unknown method " + map.get("method"));
                        sendResponse(conn, Response.error(Response.ERR_UNKNOWN_METHOD), (int) map.get("invocation_id"));
                        return null;
                    }
                } catch (Throwable ignored) {
                    // No need to report a failed salvage operation
                }
            }

            Log.err("[MlogWatcher] websocket message decoding error", th);
        }

        return null;
    }

    private void sendResponse(WebSocket conn, Response response, int invocationId) {
        try {
            response.setInvocationId(invocationId);
            conn.send(mapper.writeValueAsString(response));
        } catch (Throwable th) {
            Log.err("[MlogWatcher] error sending response", th);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.err("[MlogWatcher] socket error", ex);

        if (conn == null) {
            // This is a startup error
            open = false;
            Settings.updateWebsocketStatus(false);
        }

        if (ex instanceof BindException) {
            boolean ignore = Core.settings.getBool(Constants.Settings.ignoreServerBindError);
            if (ignore) return;

            (new Dialog(Constants.Bundles.infoServerBindErrorTitle) {
                {
                    this.getCell(this.cont).growX();
                    this.cont.margin(15.0F).add(Constants.Bundles.infoServerBindError).width(400.0F).wrap().get().setAlignment(1, 1);
                    this.buttons.button("@ok", this::hide).size(110.0F, 50.0F).pad(4.0F);
                    this.keyDown(KeyCode.enter, this::hide);
                    this.closeOnBack();
                    this.row();
                    this.check(Constants.Bundles.settingIgnoreServerBindError, (checked) -> {
                        Core.settings.put(Constants.Settings.ignoreServerBindError, checked);
                    }).checked(Core.settings.getBool(Constants.Settings.ignoreServerBindError));
                }
            }).show();
        }
    }

    @Override
    public void onStart() {
        open = true;
        Settings.updateWebsocketStatus(true);
        Log.info("[MlogWatcher] server running on port @", getPort());
    }
}
