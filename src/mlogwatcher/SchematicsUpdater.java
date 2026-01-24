package mlogwatcher;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.ui.dialogs.SchematicsDialog;

import java.lang.reflect.Method;

public class SchematicsUpdater {
    private static final String mlogWatcherTag = "MlogWatcher";

    public static boolean importSchematicsFromFile(String path) {
        try {
            Schematic schematic = Schematics.read(Fi.get(path));
            schematic.file = null;      // We don't want to keep a reference to this file
            updateSchematics(schematic, true);
            return true;
        } catch (Throwable th) {
            Log.err("[MlogWatcher] error loading schematics from file " + path, th);
            return false;
        }
    }

    public static boolean importSchematics(String encodedSchematics, boolean overwrite) {
        // The schematics file starts with "msch", which, encoded, gives this prefix
        if (!encodedSchematics.startsWith("bXNjaA")) return false;

        try {
            Schematic schematic = Schematics.readBase64(encodedSchematics);
            updateSchematics(schematic, overwrite);
            return true;
        } catch (Exception e) {
            Log.err("[MlogWatcher] error decoding schematics from message", e);
            return false;
        }
    }

    private static void updateSchematics(Schematic schematic, boolean overwrite) {
        Core.app.post(() -> {
            try {
                schematic.removeSteamID();
                schematic.labels.add(mlogWatcherTag);

                if (overwrite) {
                    Schematic existing = Vars.schematics.all()
                            .find(s -> s.name().equals(schematic.name()) && s.labels.contains(mlogWatcherTag));
                    if (existing != null) {
                        Vars.schematics.remove(existing);
                    }
                }

                Vars.schematics.add(schematic);
                Log.info("[MlogWatcher] successfully updated schematic " + schematic.name());

                // We need to update the Schematics dialog if it is shown
                SchematicsDialog dialog = Vars.ui.schematics;
                if (dialog.isShown()) {
                    Class<?> clazz = Class.forName("mindustry.ui.dialogs.SchematicsDialog");
                    Method setup = clazz.getDeclaredMethod("setup");
                    setup.setAccessible(true);
                    setup.invoke(dialog);

                    Method checkTags = clazz.getDeclaredMethod("checkTags", Schematic.class);
                    checkTags.setAccessible(true);
                    checkTags.invoke(dialog, schematic);
                }

                if (Vars.state.is(GameState.State.playing)) {
                    Vars.ui.showInfoToast("Imported schematic [gold]" + schematic.name(), 2);
                } else {
                    dialog.showInfo(schematic);
                }
            } catch (Throwable th) {
                Log.err("Error updating schematic", th);
            }
        });
    }
}
