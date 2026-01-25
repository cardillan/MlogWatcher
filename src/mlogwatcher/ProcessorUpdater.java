package mlogwatcher;

import arc.Events;
import arc.files.Fi;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.logic.LVar;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.LogicProcessor;
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
    }

    public static boolean accessible(LogicBlock.LogicBuild logicBuild) {
        return ((LogicBlock)logicBuild.block).accessible();
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

    public static ProcessorUpdateResults updateAllProcessorsOnMap(String asmCode, ProgramId newId, String variableName) {
        List<LogicProcessor> updates = new ArrayList<>();
        Vars.world.tiles.eachTile(tile -> updateTile(tile, asmCode, newId, variableName, updates));
        return new ProcessorUpdateResults(updates);
    }

    private static void updateTile(Tile tile, String asmCode, ProgramId newId, String variableName, List<LogicProcessor> updates) {
        if (tile.build instanceof LogicBlock.LogicBuild logicBuild && accessible(logicBuild)) {
            LVar lVar = logicBuild.executor.optionalVar(variableName);
            if (lVar != null && lVar.obj() instanceof String id) {
                ProgramId oldId = ProgramId.parse(id);

                final String updateStatus;
                if (oldId == null) {
                    updateStatus = LogicProcessor.MISSING_PROGRAM_ID;
                } else if (oldId.getIdPrefix().equals(newId.getIdPrefix())) {
                    if (oldId.shouldUpdate(newId)) {
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
}
