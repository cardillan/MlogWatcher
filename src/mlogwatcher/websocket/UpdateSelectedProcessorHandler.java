package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.ProcessorUpdater;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;
import mlogwatcher.websocket.api.UpdateSelectedProcessorParams;

public class UpdateSelectedProcessorHandler implements MethodHandler {
    @Override
    public Response handle(Request request) {
        if (request.getMethodVersion() != 1) {
            return Response.error(Response.ERR_UNSUPPORTED_METHOD_VERSION);
        }

        try {
            UpdateSelectedProcessorParams params = request.getParams();
            boolean success = ProcessorUpdater.insertLogic(params.getCode());
            return success ? Response.success() : Response.error(Response.ERR_NO_PROCESSOR_ATTACHED);
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error updating selected processor", e);
        }

        return Response.error(Response.ERR_INVALID_ARGUMENTS);
    }
}
