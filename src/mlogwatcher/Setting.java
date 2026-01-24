package mlogwatcher;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import mindustry.Vars;
import mlogwatcher.websocket.MlogServer;

public class Setting {
    public static void init() {
        Core.settings.defaults(
                Constants.Settings.mlogPath, "[lightgray]@none[]",
                Constants.Settings.mlogExtension, "mlog",
                Constants.Settings.mschExtension, "msch",
                Constants.Settings.websocketPort, 9992
        );

        Vars.ui.settings.addCategory("Mlog Watcher", t -> {
            Label label = new Label(() -> Core.settings.getString(Constants.Settings.mlogPath));
            label.setFontScale(0.75f);

            t.add(Constants.Bundles.settingMlogPathLabel).left().colspan(2).row();
            t.add(label).left().colspan(2).row();
            t.button(Constants.Bundles.settingMlogSelectButton, () -> {
                Vars.platform.showFileChooser(true, Constants.Bundles.settingFileChooserTitle, "*", fi -> {
                    Core.settings.put(Constants.Settings.mlogPath, fi.parent().absolutePath());
                    FileWatcher.stopWatcherThread();
                    FileWatcher.startWatcherThread();
                });
            }).height(60f).pad(16f).colspan(2).fill().row();

            TextField mlogExtField = new TextField();
            mlogExtField.update(() -> mlogExtField.setText(Core.settings.getString(Constants.Settings.mlogExtension)));
            mlogExtField.setFilter((f, c) -> c != ' ' && c != '.');
            mlogExtField.setMessageText(Constants.Bundles.settingNoExt);
            mlogExtField.changed( () -> Core.settings.put(Constants.Settings.mlogExtension, mlogExtField.getText()));
            t.add(Constants.Bundles.settingMlogExtensionInputLabel).left();
            t.add(mlogExtField).row();
            
            TextField mschExtField = new TextField();
            mschExtField.update(() -> mschExtField.setText(Core.settings.getString(Constants.Settings.mschExtension)));
            mschExtField.setFilter((f, c) -> c != ' ' && c != '.');
            mschExtField.setMessageText(Constants.Bundles.settingNoExt);
            mschExtField.changed( () -> Core.settings.put(Constants.Settings.mschExtension, mschExtField.getText()));
            t.add(Constants.Bundles.settingMschExtensionInputLabel).left();
            t.add(mschExtField).row();

            TextField portTextField = new TextField();
            portTextField.update(() -> portTextField.setText(String.valueOf(Core.settings.getInt(Constants.Settings.websocketPort))));
            portTextField.changed(() -> {
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    Core.settings.put(Constants.Settings.websocketPort, port);
                } catch (NumberFormatException ignored) {

                }
            });

            t.add(Constants.Bundles.settingWebsocketPortLabel).left();
            t.add(portTextField);
            t.row();

            t.button(Constants.Bundles.settingRestartServerButton, () -> {
                MlogServer.stopServer();
                MlogServer.startServer();
            }).height(60f).pad(16f).colspan(2).fill().row();

            t.check(Constants.Bundles.settingIgnoreServerBindError, (checked) -> {
                Core.settings.put(Constants.Settings.ignoreServerBindError, checked);
            }).colspan(2).fill().checked((x) -> Core.settings.getBool(Constants.Settings.ignoreServerBindError)).row();

            t.button("@settings.reset", () -> {
                Core.settings.remove(Constants.Settings.mlogPath);
                Core.settings.remove(Constants.Settings.mlogExtension);
                Core.settings.remove(Constants.Settings.mschExtension);
                Core.settings.remove(Constants.Settings.websocketPort);
                Core.settings.remove(Constants.Settings.ignoreServerBindError);
            }).margin(14).width(240f).pad(6).colspan(2).row();
        });
    }
}
