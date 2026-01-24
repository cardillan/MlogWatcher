package mlogwatcher.websocket;

import arc.util.Log;
import mlogwatcher.SchematicsUpdater;

public class PutSchematicInLibraryHandler implements MethodHandler {
    public static final String METHOD_NAME = "put_schematic_in_library";

    @Override
    public Response handle(Request request) {
        try {
            PutSchematicInLibraryParams params = request.getParams();

            String encodedSchematic = params.getSchematic();
            boolean success = SchematicsUpdater.importSchematics(encodedSchematic, params.isOverwrite());

            return success
                    ? Response.success("schematic updated")
                    : Response.error("failed to import schematic");
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error updating selected processor", e);
        }

        return Response.error("invalid arguments");
    }
}
