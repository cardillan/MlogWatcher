package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.SchematicsUpdater;
import mlogwatcher.websocket.api.PutSchematicInLibraryParams;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;

public class PutSchematicInLibraryHandler implements MethodHandler {
    @Override
    public Response handle(Request request) {
        try {
            PutSchematicInLibraryParams params = request.getParams();

            String encodedSchematic = params.getSchematic();
            boolean success = SchematicsUpdater.importSchematics(encodedSchematic, params.isOverwrite());

            return success ? Response.success() : Response.error(Response.ERR_SCHEMATIC_IMPORT_FAILED);
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error updating selected processor", e);
        }

        return Response.error(Response.ERR_INVALID_ARGUMENTS);
    }
}
