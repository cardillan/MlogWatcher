package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.ProcessorUpdater;
import mlogwatcher.websocket.api.ProcessorExtractResults;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;

public class ExtractSelectedProcessorCodeHandler implements MethodHandler {
    @Override
    public Response handle(Request request) {
        if (request.getMethodVersion() != 1) {
            return Response.error(Response.ERR_UNSUPPORTED_METHOD_VERSION);
        }

        try {
            String mlog = ProcessorUpdater.extractLogic();

            return mlog == null
                    ? Response.error(Response.ERR_NO_PROCESSOR_ATTACHED)
                    : Response.success(Response.RESULT_TYPE_MLOG_CODE, new ProcessorExtractResults(mlog));
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error accessing selected processor", e);
        }

        return Response.error(Response.ERR_INVALID_ARGUMENTS);
    }
}
