package mlogwatcher;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.MapInfoDialog;
import mindustry.editor.MapProcessorsDialog;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static mindustry.Vars.state;

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
        fileWatcherThread = null;
    }

    public static void restartWatcherThread() {
        stopWatcherThread();
        startWatcherThread();
    }

    public static boolean running() {
        return fileWatcherThread != null && fileWatcherThread.isAlive();
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
            Files.walkFileTree(targetFilePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    dir.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    );

                    return FileVisitResult.CONTINUE;
                }
            });

            targetFilePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            Log.info("[MlogWatcher] watching directory " + targetFilePath);
            Settings.updateWatcherStatus(true);

            while (true) {
                final WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Path path = ((Path) watchKey.watchable()).resolve((Path)event.context());

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Log.warn("[MlogWatcher] new file detected, restarting watcher thread");
                        FileWatcher.restartWatcherThread();

                        return;
                    }

                    String pathName = path.toString();
                    String fileName = pathName
                            .substring(targetFilePath.toString().length() + 1)
                            .split("\\.")[0]
                            .replaceAll("\\\\", "/");

                    Log.info("[MlogWatcher] found modified file " + pathName);

                    if (matchesExtension(pathName, Constants.Settings.mlogExtension)) {
                        Log.info("[MlogWatcher] updating logic");
                        Core.app.post(() -> {
                            String asmCode = Fi.get(pathName).readString();



                            if(Core.settings.getBool(Constants.Settings.mlogWatchByNameOn)) {
                                Log.info("[MlogWatcher] updating logic by name " + fileName);

                                outer: for(int x = 0; x < Vars.world.tiles.width; x++){
                                    for(int y = 0; y < Vars.world.tiles.height; y++){
                                        Tile tile = Vars.world.tiles.get(x, y);

                                        if(tile.isCenter() && tile.build instanceof LogicBlock.LogicBuild logicBuild && logicBuild.tag != null && logicBuild.tag.equals(fileName)) {
                                            Log.info("[MlogWatcher] found " + logicBuild);

                                            boolean prev = state.rules.editor;

                                            state.rules.editor = true;
                                            ProcessorUpdater.insertLogic(logicBuild, asmCode);
                                            state.rules.editor = prev;

                                            Reflect.invoke((MapProcessorsDialog) Reflect.get((MapInfoDialog) Reflect.get(Vars.ui.editor, "infoDialog"), "processors"), "rebuild");

                                            break outer;
                                        }
                                    }
                                }
                            } else {
                                ProcessorUpdater.insertLogic(asmCode);
                            }
                        });
                    } else if (matchesExtension(pathName, Constants.Settings.mschExtension)) {
                        Log.info("[MlogWatcher] updating schematics");
                        Core.app.post(() -> SchematicsUpdater.importSchematicsFromFile(pathName));
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
            FileWatcher.restartWatcherThread();
        } finally {
            Settings.updateWatcherStatus(false);
        }
    }

    private boolean matchesExtension(String path, String extensionKey) {
        String extension = Core.settings.getString(extensionKey);
        if (extension.isEmpty()) return false;
        return path.endsWith("." + extension);
    }
}
