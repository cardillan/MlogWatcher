package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.SchematicsUpdater;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;

public class ExtractSelectedSchematicHandler implements MethodHandler {
    @Override
    public Response handle(Request request) {
        if (request.getMethodVersion() != 1) {
            return Response.error(Response.ERR_UNSUPPORTED_METHOD_VERSION);
        }

        try {
            String encoded = SchematicsUpdater.extractSelectedSchematics();

            return encoded == null ? Response.error(Response.ERR_SCHEMATIC_EXTRACTION_FAILED)
                    : encoded.isEmpty() ? Response.error(Response.ERR_NO_SCHEMATIC_SELECTED)
                    : Response.success(encoded);
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error extracting schematic", e);
        }

        return Response.error(Response.ERR_INVALID_ARGUMENTS);
    }
}
