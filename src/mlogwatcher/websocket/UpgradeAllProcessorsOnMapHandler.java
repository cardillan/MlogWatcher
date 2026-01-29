package mlogwatcher.websocket;

import arc.util.Log;
import mindustry.Vars;
import mlogwatcher.ProcessorUpdater;
import mlogwatcher.ProcessorUpdater.VersionSelection;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.ProgramId;
import mlogwatcher.websocket.api.Request;
import mlogwatcher.websocket.api.Response;
import mlogwatcher.websocket.api.UpgradeAllProcessorsOnMapParams;

public class UpgradeAllProcessorsOnMapHandler implements MethodHandler {
    @Override
    public Response handle(Request request) {
        try {
            UpgradeAllProcessorsOnMapParams params = request.getParams();
            ProgramId newId = params.getProgramId();
            if (newId == null) return Response.error(Response.ERR_INVALID_PROGRAM_ID);

            VersionSelection versionSelection = decodeVersionMatch(params.getVersionSelection());
            if (versionSelection == null) return Response.error(Response.ERR_INVALID_VERSION_SELECTION);

            if (!Vars.state.isGame()) return Response.error(Response.ERR_NO_ACTIVE_MAP);

            ProcessorUpdateResults processorUpdateResults = ProcessorUpdater.updateAllProcessorsOnMap(
                    params.getCode(), newId, params.getVariableName(), versionSelection);

            return processorUpdateResults.getProcessorUpdates().isEmpty()
                    ? Response.error(Response.ERR_NO_PROCESSORS_FOUND)
                    : Response.success(Response.RESULT_TYPE_PROCESSOR_UPDATE, processorUpdateResults);
        } catch (ClassCastException e) {
            Log.err("[MlogWatcher] unexpected type of parameters", e);
        } catch (IllegalAccessError e) {
            Log.err("[MlogWatcher] error updating selected processor", e);
        }

        return Response.error(Response.ERR_INVALID_ARGUMENTS);
    }

    private VersionSelection decodeVersionMatch(String value) {
        for (VersionSelection match : VersionSelection.values()) {
            if (match.name().equalsIgnoreCase(value)) return match;
        }
        return null;
    }
}
