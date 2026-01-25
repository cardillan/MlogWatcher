package mlogwatcher.websocket;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.Nullable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mlogwatcher.Constants;
import mlogwatcher.ProcessorUpdater;
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

    private final Map<String, MethodHandler> handlers = new HashMap<>();

    MlogServer(int port) {
        super(new InetSocketAddress(port));

        handlers.put(Request.UPDATE_SELECTED_PROCESSOR, new UpdateSelectedProcessorHandler());
        handlers.put(Request.PUT_SCHEMATIC_IN_LIBRARY, new PutSchematicInLibraryHandler());
    }

    public static void startServer() {
        if (server != null) return;
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
            handleLegacyMessage(conn, message);
        } else if (path.equals("/v1")) {
            handleMessage(conn, message);
        } else {
            Log.err("[MlogWatcher] unknown websocket path: " + path);
        }
    }

    // Legacy
    public static final String STATUS_OK = "ok";
    public static final String STATUS_NO_PROCESSOR = "no_processor";

    private void handleLegacyMessage(WebSocket conn, String message) {
        boolean processorAttached = ProcessorUpdater.insertLogic(message);
        conn.send(processorAttached ? STATUS_OK : STATUS_NO_PROCESSOR);
    }

    private void handleMessage(WebSocket conn, String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Response response;
            Request request = mapper.readValue(message, Request.class);

            MethodHandler handler = handlers.get(request.getMethod());
            if (handler == null) {
                Log.err("[MlogWatcher] unknown method " + request.getMethod());
                response = Response.error(Response.ERR_UNKNOWN_METHOD);
            } else {
                response = handler.handle(request);
            }

            response.setInvocationId(request.getInvocationId());
            conn.send(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            Log.err("[MlogWatcher] json processing error", e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.err("[MlogWatcher] socket error", ex);

        if (ex instanceof BindException) {
            boolean ignore = Core.settings.getBool(Constants.Settings.ignoreServerBindError);
            if(ignore) return;

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
        Log.info("[MlogWatcher] server running on port @", getPort());
    }
}
