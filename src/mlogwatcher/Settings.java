package mlogwatcher;

import arc.Core;
import arc.graphics.Color;
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

            t.add(Constants.Bundles.settingMlogPathLabel).left().colspan(columns).row();
            TextField watchedDirectory = new TextField();
            watchedDirectory.setMessageText(Constants.Bundles.settingNoPathSelected);
            watchedDirectory.update(() -> watchedDirectory.setText(String.valueOf(Core.settings.getString(Constants.Settings.mlogPath))));
            watchedDirectory.changed(() -> {
                Core.settings.put(Constants.Settings.mlogPath, watchedDirectory.getText());
                FileWatcher.stopWatcherThread();
                FileWatcher.startWatcherThread();
            });
            t.add(watchedDirectory).colspan(columns).left().growX().row();

            t.button(Constants.Bundles.settingMlogSelectButton, () -> {
                Vars.platform.showFileChooser(true, Constants.Bundles.settingFileChooserTitle, "*", fi -> {
                    String path = fi.parent().absolutePath();
                    Core.settings.put(Constants.Settings.mlogPath, path);
                    FileWatcher.stopWatcherThread();
                    FileWatcher.startWatcherThread();
                    watchedDirectory.setText(path);
                });
            }).height(60f).pad(16f).colspan(columns).width(t.getWidth()).fill().row();

            TextField mlogExtField = new TextField();
            mlogExtField.update(() -> mlogExtField.setText(Core.settings.getString(Constants.Settings.mlogExtension)));
            mlogExtField.setFilter((f, c) -> c != ' ' && c != '.');
            mlogExtField.setMessageText(Constants.Bundles.settingNoExt);
            mlogExtField.changed(() -> Core.settings.put(Constants.Settings.mlogExtension, mlogExtField.getText()));
            t.add(Constants.Bundles.settingMlogExtensionInputLabel).left();
            t.add(mlogExtField).row();

            TextField mschExtField = new TextField();
            mschExtField.update(() -> mschExtField.setText(Core.settings.getString(Constants.Settings.mschExtension)));
            mschExtField.setFilter((f, c) -> c != ' ' && c != '.');
            mschExtField.setMessageText(Constants.Bundles.settingNoExt);
            mschExtField.changed(() -> Core.settings.put(Constants.Settings.mschExtension, mschExtField.getText()));
            t.add(Constants.Bundles.settingMschExtensionInputLabel).left();
            t.add(mschExtField).row();

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
            t.add(portTextField);
            t.row();

            t.left();
            t.check(Constants.Bundles.settingIgnoreServerBindError, (checked) -> {
                        Core.settings.put(Constants.Settings.ignoreServerBindError, checked);
                    }).colspan(columns).growX().left()
                    .checked(x -> Core.settings.getBool(Constants.Settings.ignoreServerBindError)).row();
            t.center();

            t.button(Constants.Bundles.settingRestartServerButton, () -> {
                MlogServer.stopServer();
                MlogServer.startServer();
            }).height(60f).pad(16f).colspan(columns).growX().row();

            category(t, Constants.DirectBundles.settingsProcessorId, false);

            t.add(Constants.Bundles.settingProcessorTagVariables).colspan(columns).left().row();
            TextField processorIds = new TextField();
            processorIds.update(() -> processorIds.setText(String.valueOf(Core.settings.getString(Constants.Settings.processorTagVariables))));
            processorIds.changed(() -> {
                Core.settings.put(Constants.Settings.processorTagVariables, processorIds.getText());
                ProcessorIdLabel.updateVariables();
            });
            processorIds.setWidth(t.getWidth());
            t.add(processorIds).colspan(columns).left().growX().row();

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
                Core.settings.remove(Constants.Settings.processorTagVariables);

                FileWatcher.stopWatcherThread();
                FileWatcher.startWatcherThread();
                MlogServer.stopServer();
                MlogServer.startServer();
                ProcessorIdLabel.updateVariables();
            }).margin(14).width(240f).pad(6).padTop(12).colspan(columns).row();
        });
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
