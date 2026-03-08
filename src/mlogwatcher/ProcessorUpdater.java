package mlogwatcher;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mlogwatcher.websocket.api.LogicProcessor;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.ProgramId;

import java.util.ArrayList;
import java.util.List;

public class ProcessorUpdater {
    @Nullable
    private static LogicBlock.LogicBuild lastTappedLogicBuild = null;

    public static void init() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof LogicBlock.LogicBuild logicBuild && accessible(logicBuild)) {
                lastTappedLogicBuild = logicBuild;
            } else {
                lastTappedLogicBuild = null;
            }
        });

        Events.run(EventType.Trigger.draw, () -> {
            if (lastTappedLogicBuild == null || lastTappedLogicBuild.dead) return;
            Draw.reset();
            Lines.stroke(1f);
            Draw.color(Pal.accent);
            Lines.poly(lastTappedLogicBuild.x, lastTappedLogicBuild.y, 4, 8f);
            Draw.reset();
        });

        Events.on(EventType.ResetEvent.class, e -> lastTappedLogicBuild = null);
    }

    public static boolean accessible(LogicBlock.LogicBuild logicBuild) {
        return ((LogicBlock) logicBuild.block).accessible();
    }

    public static void insertLogicFromFile(String path) {
        String asmCode = Fi.get(path).readString();
        insertLogic(lastTappedLogicBuild, asmCode);
    }

    public static boolean insertLogic(String asmCode) {
        return insertLogic(lastTappedLogicBuild, asmCode);
    }

    public static boolean insertLogic(LogicBlock.LogicBuild build, String asmCode) {
        if (build == null || build.dead || !accessible(build)) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return false;
        }

        build.configure(LogicBlock.compress(asmCode, build.relativeConnections()));
        Fx.spawn.at(build.x, build.y);
        Log.info("[MlogWatcher] successfully injected code into logic block");
        return true;
    }

    public static String extractLogic() {
        return extractLogic(lastTappedLogicBuild);
    }

    private static String extractLogic(LogicBlock.LogicBuild build) {
        if (build == null || build.dead || !accessible(build)) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return null;
        } else {
            Fx.colorTrail.at(build.x, build.y, 4f * build.block.size, Color.gold);
            return build.code;
        }
    }

    public enum VersionSelection {exact, compatible, any}

    public static ProcessorUpdateResults updateAllProcessorsOnMap(String asmCode, ProgramId newId, String variableName,
            VersionSelection versionSelection) {
        List<LogicProcessor> updates = new ArrayList<>();
        Vars.world.tiles.eachTile(tile -> updateTile(tile, asmCode, newId, variableName, versionSelection, updates));
        return new ProcessorUpdateResults(updates);
    }

    private static void updateTile(Tile tile, String asmCode, ProgramId newId, String variableName,
            VersionSelection versionSelection, List<LogicProcessor> updates) {
        if (tile.build instanceof LogicBlock.LogicBuild logicBuild && accessible(logicBuild)) {
            String id = extractString(logicBuild.executor, variableName);
            if (id != null) {
                ProgramId oldId = ProgramId.parse(id);

                final String updateStatus;
                if (oldId == null) {
                    updateStatus = LogicProcessor.MISSING_PROGRAM_ID;
                } else if (oldId.getIdPrefix().equals(newId.getIdPrefix())) {
                    if (shouldUpdate(oldId, newId, versionSelection)) {
                        insertLogic(logicBuild, asmCode);
                        updateStatus = LogicProcessor.UPDATED;
                    } else {
                        updateStatus = LogicProcessor.INCOMPATIBLE_VERSION;
                    }
                } else {
                    return;
                }

                updates.add(new LogicProcessor(logicBuild.tile.x, logicBuild.tile.y, logicBuild.block.name, oldId, updateStatus));
            }
        }
    }

    public static boolean shouldUpdate(ProgramId oldId, ProgramId newId, VersionSelection versionSelection) {
        return oldId.getIdPrefix().equals(newId.getIdPrefix()) && switch (versionSelection) {
            case exact -> oldId.exactVersionMatch(newId);
            case compatible -> oldId.compatibleVersionMatch(newId);
            case any -> true;
        };
    }

    private static @Nullable String extractString(LExecutor executor, String variableName) {
        LVar lVar = executor.optionalVar(variableName);
        return lVar != null && lVar.obj() instanceof String str ? str : null;
    }
}
