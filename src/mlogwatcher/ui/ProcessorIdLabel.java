package mlogwatcher.ui;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.WorldLabel;
import mindustry.ui.Fonts;
import mindustry.world.blocks.logic.LogicBlock;
import mlogwatcher.Constants;
import mlogwatcher.ProcessorUpdater;

public class ProcessorIdLabel {
    public static Seq<String> variables = new Seq<>();

    static WorldLabel label;

    public static void init() {
        Events.on(EventType.ResetEvent.class, e -> { if (label != null) label.remove(); });

        Events.run(EventType.Trigger.update, () -> {
            if (label == null) {
                label = WorldLabel.create();
            }
            float x = Core.input.mouseWorldX();
            float y = Core.input.mouseWorldY();

            if (Vars.world.buildWorld(x, y) instanceof LogicBlock.LogicBuild processor) {
                for (String variable : variables) {
                    String text = ProcessorUpdater.extractString(processor.executor, variable);
                    if (text != null && !text.isEmpty()) {
                        updateLabel(processor, text);
                        return;
                    }
                }
            }

            label.remove();
        });
    }

    public static void updateVariables() {
        String[] list = Core.settings.getString(Constants.Settings.processorTagVariables, "*id").split(" ");
        variables.clear();
        for (String variable : list) {
            variables.add(variable);
        }
    }

    static LogicBlock.LogicBuild lastBuild;
    static String lastText;
    static char[] buffer = new char[150];

    private static void updateLabel(LogicBlock.LogicBuild processor, String text) {
        if (processor == lastBuild && text == lastText) {
            label.add();
            return;
        }

        boolean copying = false;
        int beg = 0, l = 0;
        int stop = Math.min(text.length(), buffer.length);

        for (int i = 0; i < stop; i++) {
            char ch = text.charAt(i);
            buffer[l++] = ch;

            if (ch == ' ' && !copying) {
                l = beg;
                copying = true;
            } else if (ch == '\n') {
                copying = false;
                beg = l;
            }
        }

        label.flags = WorldLabel.flagBackground | WorldLabel.flagOutline;
        String str = new String(buffer, 0, l);

        // Here we'll compute the position
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.25F * label.fontSize / Scl.scl(1.0F));
        layout.setText(font, text);
        int border = (label.flags & WorldLabel.flagBackground) != 0 ? 1 : 0;
        float x = processor.x;
        float y = processor.y - processor.block.size * Vars.tilesize * 0.5f - layout.height / 2f - border * 1.5F;
        Pools.free(layout);
        font.getData().setScale(1.0F);
        font.setUseIntegerPositions(ints);

        lastText = text;
        lastBuild = processor;
        label.text = str;
        label.set(x, y);
        label.add();
    }
}
