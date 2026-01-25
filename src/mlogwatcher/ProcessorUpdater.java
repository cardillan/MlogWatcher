package mlogwatcher;

import arc.Events;
import arc.files.Fi;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;

public class ProcessorUpdater {
    @Nullable
    private static LogicBlock.LogicBuild lastTappedLogicBuild = null;

    public static void init() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof LogicBlock.LogicBuild logicBuild) {
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

    public static void insertLogicFromFile(String path) {
        String asmCode = Fi.get(path).readString();
        insertLogic(asmCode);
    }

    public static boolean insertLogic(String asmCode) {
        if (lastTappedLogicBuild == null || lastTappedLogicBuild.dead) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return false;
        }

        lastTappedLogicBuild.configure(LogicBlock.compress(asmCode, lastTappedLogicBuild.relativeConnections()));
        Fx.spawn.at(lastTappedLogicBuild.x, lastTappedLogicBuild.y);
        Log.info("[MlogWatcher] successfully injected code into logic block");
        return true;
    }
}
