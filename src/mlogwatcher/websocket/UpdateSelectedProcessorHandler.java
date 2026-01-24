package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.ProcessorUpdater;

import java.util.Base64;

public class UpdateSelectedProcessorHandler implements MethodHandler {
    public static final String METHOD_NAME = "update_selected_processor";

    @Override
    public Response handle(Request request) {
        try {
            UpdateSelectedProcessorParams params = request.getParams();

            byte[] bytes = Base64.getDecoder().decode(params.getCode());
            String code = new String(bytes);

            boolean processorAttached = ProcessorUpdater.insertLogic(code);
            return processorAttached
                    ? Response.success("processor updated")
                    : Response.error("no processor attached");
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error updating selected processor", e);
        }

        return Response.error("invalid arguments");
    }
}
