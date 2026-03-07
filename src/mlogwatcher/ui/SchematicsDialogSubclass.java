package mlogwatcher.ui;

import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.ui.dialogs.SchematicsDialog;

public class SchematicsDialogSubclass extends SchematicsDialog {
    public Schematic lastSelectedSchematic;

    @Override
    public void showInfo(Schematic schematic) {
        Log.info("Displaying schematic: " + schematic.name());
        lastSelectedSchematic = schematic;
        super.showInfo(schematic);
    }

    public static void init() {
        SchematicsDialog previous = Vars.ui.schematics;
        Vars.ui.schematics = new SchematicsDialogSubclass();
        Log.info("Replaced " + previous.getClass().getSimpleName() + " with " + Vars.ui.schematics.getClass().getSimpleName());

        previous.shown(() -> Log.info("Original schematics screen shown"));
        Vars.ui.schematics.shown(() -> Log.info("Overridden schematics screen shown"));
    }
}
