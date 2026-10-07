package com.improvedchat;

import com.improvedchat.overlay.OverlayConfig;
import com.improvedchat.overlay.OverlayFilterMode;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

/** Headless interaction/layout checks against the real Swing panel. */
public class ImprovedChatPanelTest {
    private static final class FakePlugin extends ImprovedChatPlugin {
        final List<OverlayConfig> configs = new ArrayList<>();
        boolean globalOverride;
        FakePlugin() { configs.add(OverlayConfig.preset("Game Alerts", "game")); }
        @Override public List<OverlayConfig> getOverlayConfigs() { return configs; }
        @Override public void onOverlayConfigChanged() { }
        @Override public int getOverlayWidth(OverlayConfig config) { return config.getWidgetWidth(); }
        @Override public boolean isGlobalFilterOverride() { return globalOverride; }
        @Override public boolean isGlobalFilterAvailable() { return false; }
        @Override public String movementHint() { return "Hold Alt and drag the overlay to move it."; }
    }

    @Test public void editorPreservesScrollAndDisablesOverriddenFilters() throws Exception {
        FakePlugin plugin = new FakePlugin();
        ImprovedChatPanel[] holder = new ImprovedChatPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new ImprovedChatPanel(plugin);
            holder[0].setSize(280, 900);
            button(holder[0], "EDIT").doClick();
            layout(holder[0]);
        });
        SwingUtilities.invokeAndWait(() -> {
            layout(holder[0]);
            JScrollPane scroll = findScroll(holder[0]);
            scroll.getVerticalScrollBar().setValue(200);
            assertEquals(200, scroll.getVerticalScrollBar().getValue());
            button(holder[0], "Background").doClick();
            layout(holder[0]);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(200, findScroll(holder[0]).getVerticalScrollBar().getValue());
            plugin.globalOverride = true;
            plugin.configs.get(0).setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES);
            holder[0].rebuild();
            layout(holder[0]);
            assertFalse(button(holder[0], "Use RuneLite Chat Filter").isEnabled());
            assertFalse(button(holder[0], "TEST CUSTOM RULES").isEnabled());
        });
    }

    @Test public void renderPanelPreviews() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                FakePlugin plugin = new FakePlugin();
                ImprovedChatPanel panel = new ImprovedChatPanel(plugin);
                panel.setSize(280, 900);
                layout(panel);
                capture(panel, "overlay-list");
                button(panel, "EDIT").doClick();
                layout(panel);
                capture(panel, "overlay-editor");
                plugin.configs.get(0).setFilterMode(OverlayFilterMode.SHOW_ONLY_MATCHES);
                plugin.configs.get(0).setFilteredRegex("[invalid");
                panel.rebuild();
                button(panel, "MESSAGE FILTERING").doClick();
                layout(panel);
                Component filtering = button(panel, "MESSAGE FILTERING");
                javax.swing.JComponent section = (javax.swing.JComponent) filtering.getParent();
                section.scrollRectToVisible(new java.awt.Rectangle(0, 0, section.getWidth(), 700));
                capture(panel, "overlay-filtering");
                panel.setSize(225, 900);
                panel.rebuild();
                layout(panel);
                capture(panel, "overlay-narrow");
            } catch (Exception ex) { throw new AssertionError(ex); }
        });
    }

    private static void capture(Component panel, String name) throws Exception {
        File directory = new File("build/reports/ui");
        directory.mkdirs();
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        panel.printAll(graphics);
        graphics.dispose();
        ImageIO.write(image, "png", new File(directory, name + ".png"));
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container) layout((Container) child);
    }

    private static JScrollPane findScroll(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JScrollPane) return (JScrollPane) child;
        }
        throw new AssertionError("Missing page scroll pane");
    }

    private static AbstractButton button(Container parent, String text) {
        AbstractButton result = findButton(parent, text);
        if (result == null) throw new AssertionError("Missing button " + text);
        return result;
    }

    private static AbstractButton findButton(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof AbstractButton && ((AbstractButton) child).getText().contains(text)) return (AbstractButton) child;
            if (child instanceof Container) {
                AbstractButton found = findButton((Container) child, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
