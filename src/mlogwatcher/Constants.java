package mlogwatcher;

public class Constants {
    public static class Bundles {
        public static final String settingsFileWatcher = "mlogwatcher.setting.fileWatcher";
        public static final String settingMlogPathLabel = "@mlogwatcher.setting.mlogLabel";
        public static final String settingNoPathSelected = "@mlogwatcher.setting.noPath";
        public static final String settingMlogSelectButton = "@mlogwatcher.setting.mlogSelectButton";
        public static final String settingFileChooserTitle = "@mlogwatcher.setting.fileChooserTitle";
        public static final String settingMlogExtensionInputLabel = "@mlogwatcher.setting.mlogExtensionInputLabel";
        public static final String settingMschExtensionInputLabel = "@mlogwatcher.setting.mschExtensionInputLabel";
        public static final String settingNoExt = "@mlogwatcher.setting.noExtension";

        public static final String settingsWebSocket = "mlogwatcher.setting.websocket";
        public static final String settingWebsocketPortLabel = "@mlogwatcher.setting.websocketPortLabel";
        public static final String settingRestartServerButton = "@mlogwatcher.setting.restartServerButton";
        public static final String settingIgnoreServerBindError = "@mlogwatcher.setting.ignoreServerBindError";

        public static final String settingsProcessorId = "mlogwatcher.setting.processorId";
        public static final String settingProcessorTagVariables = "@mlogwatcher.setting.processorTagVariables";

        public static final String infoServerBindErrorTitle = "@mlogwatcher.info.serverBindErrorTitle";
        public static final String infoServerBindError = "@mlogwatcher.info.serverBindError";
    }

    public static class Settings {
        public static final String mlogPath = "mlogwatcher-mlog-path";
        public static final String mlogExtension = "mlogwatcher-mlog-extension";
        public static final String mschExtension = "mlogwatcher-msch-extension";
        public static final String websocketPort = "mlogwatcher-websocket-port";
        public static final String ignoreServerBindError = "mlogwatcher-ignore-websocket-error";
        public static final String processorTagVariables = "mlogwatcher-processor-tag-variables";
    }
}
