package mlogwatcher;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Icon;
import mlogwatcher.ui.ProcessorIdLabel;
import mlogwatcher.websocket.MlogServer;

import static arc.Core.bundle;
import static mindustry.Vars.ui;

public class Settings {
    private static final int columns = 2;
    private static Cell<Label> numberOfSchematicsLabel;

    public static void init() {
        Core.settings.defaults(
                Constants.Settings.mlogPath, "",
                Constants.Settings.mlogExtension, "mlog",
                Constants.Settings.mschExtension, "msch",
                Constants.Settings.websocketPort, 9992,
                Constants.Settings.processorTagVariables, "*tag *id"
        );

        Vars.ui.settings.addCategory("Mlog Watcher", Icon.logic, t -> {
            category(t, Constants.DirectBundles.settingsFileWatcher, true);

            TextField watchedDirectory = wideText(t, Constants.Settings.mlogPath,
                    Constants.Bundles.settingMlogPathLabel,
                    Constants.Bundles.settingNoPathSelected,
                    c -> FileWatcher.restartWatcherThread());

            button(t, Constants.Bundles.settingMlogSelectButton, () ->
                    Vars.platform.showFileChooser(true, Constants.Bundles.settingFileChooserTitle, "*", fi -> {
                        String path = fi.parent().absolutePath();
                        Core.settings.put(Constants.Settings.mlogPath, path);
                        FileWatcher.restartWatcherThread();
                        watchedDirectory.setText(path);
                    }));

            extension(t, Constants.Settings.mlogExtension, Constants.Bundles.settingMlogExtensionInputLabel);
            extension(t, Constants.Settings.mschExtension, Constants.Bundles.settingMschExtensionInputLabel);

            category(t, Constants.DirectBundles.settingsWebSocket, false);

            TextField portTextField = new TextField();
            portTextField.update(() -> portTextField.setText(String.valueOf(Core.settings.getInt(Constants.Settings.websocketPort))));
            portTextField.changed(() -> {
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    Core.settings.put(Constants.Settings.websocketPort, port);
                    MlogServer.stopServer();
                    MlogServer.startServer();
                } catch (NumberFormatException ignored) {
                }
            });

            t.add(Constants.Bundles.settingWebsocketPortLabel).left();
            t.add(portTextField).row();

            check(t, Constants.Settings.ignoreServerBindError, Constants.Bundles.settingIgnoreServerBindError);
            check(t, Constants.Settings.legacyApiOff, Constants.Bundles.settingLegacyApiOff);

            button(t, Constants.Bundles.settingRestartServerButton, MlogServer::restartServer);

            category(t, Constants.DirectBundles.settingsProcessorId, false);

            wideText(t, Constants.Settings.processorTagVariables,
                    Constants.Bundles.settingProcessorTagVariables,
                    Constants.Bundles.settingNoTagVariables,
                    c -> ProcessorIdLabel.updateVariables());

            category(t, Constants.DirectBundles.schematicLibrary, false);
            numberOfSchematicsLabel = t.add(numberOfSchematicsText()).left().colspan(columns).color(Color.gray);
            numberOfSchematicsLabel.row();
            t.button(Constants.Bundles.purgeSchematics, () -> {
                ui.showConfirm("@confirm", Constants.Bundles.purgeSchematicsPrompt, SchematicsUpdater::purgeSchematics);
                numberOfSchematicsLabel.update(l -> l.setText(numberOfSchematicsText()));
            }).height(60f).pad(16f).colspan(columns).growX().row();


            t.button("@settings.reset", () -> {
                Core.settings.remove(Constants.Settings.mlogPath);
                Core.settings.remove(Constants.Settings.mlogExtension);
                Core.settings.remove(Constants.Settings.mschExtension);
                Core.settings.remove(Constants.Settings.websocketPort);
                Core.settings.remove(Constants.Settings.ignoreServerBindError);
                Core.settings.remove(Constants.Settings.legacyApiOff);
                Core.settings.remove(Constants.Settings.processorTagVariables);

                FileWatcher.restartWatcherThread();
                MlogServer.restartServer();
                ProcessorIdLabel.updateVariables();
            }).margin(14).width(240f).pad(6).padTop(12).colspan(columns).row();
        });
    }

    private static TextField wideText(Table t, String key, String name, String message, Cons<String> changed) {
        t.add(name).left().colspan(columns).row();
        TextField field = new TextField();
        field.setMessageText(message);
        field.update(() -> field.setText(String.valueOf(Core.settings.getString(key))));
        field.changed(() -> {
            Core.settings.put(key, field.getText());
            changed.get(field.getText());
        });
        t.add(field).colspan(columns).left().growX().row();
        return field;
    }

    private static void extension(Table t, String key, String name) {
        t.add(name).left();
        TextField field = new TextField();
        field.setFilter((f, c) -> c != ' ' && c != '.');
        field.setMessageText(Constants.Bundles.settingNoExt);
        field.update(() -> field.setText(String.valueOf(Core.settings.getString(key))));
        field.changed(() -> Core.settings.put(key, field.getText()));
        t.add(field).row();
    }

    private static void check(Table t, String key, String name) {
        Cell<CheckBox> check = t.check(name, c -> Core.settings.put(key, c))
                .checked(c -> Core.settings.getBool(key))
                .colspan(columns).growX().padBottom(2f).checked(x -> Core.settings.getBool(key));
        check.get().left();
        check.row();
    }

    private static void button(Table t, String name, Runnable runnable) {
        t.button(name, runnable).height(60f).pad(16f).colspan(columns).width(t.getWidth()).fill().row();
    }

    public static void update() {
        if (ui.settings.isShown() && numberOfSchematicsLabel != null) {
            numberOfSchematicsLabel.update(l -> l.setText(numberOfSchematicsText()));
        }
    }

    private static void category(Table t, String key, boolean first) {
        t.add(bundle.get(key, ""))
                .left()
                .color(Color.gray)
                .colspan(columns).pad(10).padTop(first ? 0 : 40).padBottom(4).row();
        t.image().color(Color.gray).fillX().height(3)
                .pad(0).colspan(columns).padTop(0).padBottom(20).row();
    }

    private static String numberOfSchematicsText() {
        return Core.bundle.get(Constants.DirectBundles.numberOfSchematics) + " [white]" + SchematicsUpdater.numberOfSchematics();
    }
}
