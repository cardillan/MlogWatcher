package mlogwatcher;

import arc.Core;
import arc.util.Log;
import arc.util.Nullable;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher {
    @Nullable
    private static Thread fileWatcherThread;

    public static void startWatcherThread() {
        String path = Core.settings.getString(Constants.Settings.mlogPath);
        if (fileWatcherThread != null || path.isEmpty()) return;
        fileWatcherThread = new FileWatcherThread(path);
        fileWatcherThread.start();
    }

    public static void stopWatcherThread() {
        if (fileWatcherThread == null) return;
        fileWatcherThread.interrupt();
    }

    public static void restartWatcherThread() {
        stopWatcherThread();
        startWatcherThread();
    }
}

class FileWatcherThread extends Thread {
    private final String targetFilePath;

    public FileWatcherThread(String targetFilePath) {
        super();
        setDaemon(true);
        setPriority(MIN_PRIORITY);
        this.targetFilePath = targetFilePath;
    }

    @Override
    public void run() {
        super.run();
        Path targetFilePath = FileSystems.getDefault().getPath(this.targetFilePath);
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            targetFilePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            Log.info("[MlogWatcher] watching directory " + targetFilePath);
            while (true) {
                final WatchKey watchKey = watchService.take();
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String path = targetFilePath.resolve((Path)event.context()).toString();
                    Log.info("[MlogWatcher] found modified file " + path);
                    if (matchesExtension(path, Constants.Settings.mlogExtension)) {
                        Log.info("[MlogWatcher] updating logic");
                        Core.app.post(() -> ProcessorUpdater.insertLogicFromFile(path));
                    } else if (matchesExtension(path, Constants.Settings.mschExtension)) {
                        Log.info("[MlogWatcher] updating schematics");
                        Core.app.post(() -> SchematicsUpdater.importSchematicsFromFile(path));
                    }
                }

                boolean valid = watchKey.reset();
                if (!valid) throw new Exception("watch mode no longer valid!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.warn("[MlogWatcher] directory has been changed");
        } catch (Exception e) {
            Log.warn("[MlogWatcher] file watcher restarted");
            FileWatcher.startWatcherThread();
        }
    }

    private boolean matchesExtension(String path, String extensionKey) {
        String extension = Core.settings.getString(extensionKey);
        if (extension.isEmpty()) return false;
        return path.endsWith("." + extension);
    }
}
