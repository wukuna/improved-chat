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
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

/** Headless interaction/layout checks against the real Swing panel. */
public class ImprovedChatPanelTest {
    private static final class FakePlugin extends ImprovedChatPlugin {
        final List<OverlayConfig> configs = new ArrayList<>();
        boolean globalOverride;
        int saves;
        FakePlugin() { configs.add(OverlayConfig.preset("Game Alerts", "game")); }
        @Override public List<OverlayConfig> getOverlayConfigs() { return configs; }
        @Override public void onOverlayConfigChanged() { saves++; }
        @Override public int getOverlayWidth(OverlayConfig config) { return config.getWidgetWidth(); }
        @Override public net.runelite.client.config.Notification customizeNotification(net.runelite.client.config.Notification original) {
            return com.improvedchat.overlay.OverlayNotifications.normalize(original.withOverride(true).withInitialized(true).withVolume(50));
        }
        @Override public boolean isGlobalFilterOverride() { return globalOverride; }
        @Override public boolean isGlobalFilterAvailable() { return false; }
        @Override public String movementHint() { return "Hold Alt and drag the overlay to move it."; }
    }

    @Test public void editorPreservesScrollAndDisablesOverriddenFilters() throws Exception {
        FakePlugin plugin = new FakePlugin();
        ImprovedChatPanel[] holder = new ImprovedChatPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new ImprovedChatPanel(plugin);
            holder[0].setSize(280, 650);
            editButtons(holder[0]).get(0).doClick();
            layout(holder[0]);
        });
        SwingUtilities.invokeAndWait(() -> {
            layout(holder[0]);
            JScrollPane scroll = findScroll(holder[0]);
            scroll.getVerticalScrollBar().setValue(50);
            assertEquals(50, scroll.getVerticalScrollBar().getValue());
            tabs(holder[0]).setSelectedIndex(1);
            button(holder[0], "Background").doClick();
            layout(holder[0]);
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(50, findScroll(holder[0]).getVerticalScrollBar().getValue());
            tabs(holder[0]).setSelectedIndex(0);
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
                panel.setSize(280, 650);
                layout(panel);
                capture(panel, "overlay-list");
                editButtons(panel).get(0).doClick();
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

    @Test public void notificationsEditorKeepsEachOverlayIndependent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                FakePlugin plugin=new FakePlugin(); OverlayConfig second=new OverlayConfig(); plugin.configs.add(second);
                ImprovedChatPanel panel=new ImprovedChatPanel(plugin); panel.setSize(280,900);
                editButtons(panel).get(0).doClick(); tabs(panel).setSelectedIndex(2);
                button(panel,"Enable Notifications").doClick();
                assertTrue(plugin.configs.get(0).getNotification().isEnabled()); assertFalse(second.getNotification().isEnabled());
                button(panel,"Customize Notification").doClick();
                assertTrue(plugin.configs.get(0).getNotification().isOverride());
                layout(panel);
                javax.swing.JComponent section=(javax.swing.JComponent)tabs(panel).getSelectedComponent();
                section.scrollRectToVisible(new java.awt.Rectangle(0,0,section.getWidth(),650));
                capture(panel,"overlay-notifications");
                button(panel,"Enable Notifications").doClick();
                assertFalse(plugin.configs.get(0).getNotification().isEnabled());
            } catch (Exception ex) { throw new AssertionError(ex); }
        });
    }

    @Test public void tabsRetainSelectionDuringConditionalRebuildAndAutoSaveText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FakePlugin plugin = new FakePlugin();
            ImprovedChatPanel panel = new ImprovedChatPanel(plugin);
            panel.setSize(225, 900); layout(panel);
            assertEquals(3, tabs(panel).getTabCount());
            assertEquals("Messages", tabs(panel).getTitleAt(0));
            assertEquals("Appearance", tabs(panel).getTitleAt(1));
            assertEquals("Notifications", tabs(panel).getTitleAt(2));
            javax.swing.JTextField name = namedField(panel, "Name");
            name.setText("Renamed without pressing Enter");
            assertEquals("Renamed without pressing Enter", plugin.configs.get(0).getName());
            assertTrue(plugin.saves > 0);
            tabs(panel).setSelectedIndex(1);
            button(panel, "Background").doClick();
            assertEquals(1, tabs(panel).getSelectedIndex());
            assertTrue(plugin.configs.get(0).isBackgroundEnabled());
            assertFalse(button(panel, "Use RuneLite Chat Filter").isShowing());
            panel.rebuild();
            assertEquals(1, tabs(panel).getSelectedIndex());
        });
    }

    @Test public void expandingMessagesAdjustsActivePageHeight() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FakePlugin plugin = new FakePlugin();
            ImprovedChatPanel panel = new ImprovedChatPanel(plugin);
            panel.setSize(225, 650); layout(panel);
            int before = tabs(panel).getPreferredSize().height;
            button(panel, "MESSAGE HIGHLIGHTS").doClick();
            layout(panel);
            assertTrue(tabs(panel).getPreferredSize().height > before);
            assertEquals(0, tabs(panel).getSelectedIndex());
        });
    }

    @Test public void threeOverlaysStayListedAndEachEditorKeepsItsOwnTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                FakePlugin plugin = new FakePlugin();
                plugin.configs.add(OverlayConfig.preset("Private Messages", "private"));
                plugin.configs.add(OverlayConfig.preset("Clan Highlights", "clan"));
                ImprovedChatPanel panel = new ImprovedChatPanel(plugin);
                panel.setSize(280, 900); layout(panel);
                assertEquals(3, editButtons(panel).size());
                tabs(panel).setSelectedIndex(1);
                editButtons(panel).get(1).doClick();
                assertEquals(0, tabs(panel).getSelectedIndex());
                namedField(panel,"Name").setText("Private Only");
                assertEquals("Game Alerts", plugin.configs.get(0).getName());
                assertEquals("Private Only", plugin.configs.get(1).getName());
                tabs(panel).setSelectedIndex(2);
                button(panel,"Enable Notifications").doClick();
                assertTrue(plugin.configs.get(1).getNotification().isEnabled());
                assertFalse(plugin.configs.get(0).getNotification().isEnabled());
                editButtons(panel).get(0).doClick();
                assertEquals(1, tabs(panel).getSelectedIndex());
                layout(panel); capture(panel,"three-overlays-appearance");
                panel.setSize(225,900); panel.rebuild(); layout(panel); capture(panel,"three-overlays-narrow");
            } catch (Exception e) { throw new AssertionError(e); }
        });
    }

    @Test public void betaReplacementEditorSavesMultipleRulesWithoutLosingAnotherRule() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FakePlugin plugin = new FakePlugin();
            ImprovedChatPanel panel = new ImprovedChatPanel(plugin); panel.setSize(225,900);
            button(panel,"MESSAGE REPLACER (BETA)").doClick();
            button(panel,"Enable message replacer (Beta)").doClick();
            button(panel,"ADD REPLACEMENT").doClick();
            namedField(panel,"Original message").setText("Your thrall returns to the grave.");
            namedField(panel,"Show instead").setText("You need to summon a thrall!");
            javax.swing.JTextField staleField = namedField(panel,"Original message");
            button(panel,"ADD REPLACEMENT").doClick();
            List<javax.swing.JTextField> originals = namedFields(panel,"Original message");
            List<javax.swing.JTextField> replacements = namedFields(panel,"Show instead");
            assertEquals(2, originals.size());
            originals.get(1).setText("Your boost expired."); replacements.get(1).setText("Drink another potion!");
            button(panel,"REMOVE REPLACEMENT").doClick();
            // A detached field's old callback must not edit the newly first rule after removal.
            staleField.setText("detached editor callback");
            OverlayConfig config = plugin.configs.get(0);
            assertEquals(1,config.getTextReplacements().size());
            assertEquals("Your boost expired.",config.getTextReplacements().get(0).getOriginal());
            assertEquals("Drink another potion!",com.improvedchat.overlay.OverlayTextReplacements.displayText(config,"Your boost expired."));
            namedField(panel,"Test message text").setText("Your boost expired.");
            button(panel,"TEST REPLACEMENTS").doClick();
            layout(panel);
            assertTrue(plugin.saves > 0);
            try { capture(panel,"message-replacer-beta"); } catch(Exception e) {throw new AssertionError(e);}
        });
    }

    private static List<javax.swing.JTextField> namedFields(Container parent, String name) {
        List<javax.swing.JTextField> result = new ArrayList<>();
        for(Component child:parent.getComponents()) {
            if(child instanceof javax.swing.JTextField && name.equals(child.getName())) result.add((javax.swing.JTextField)child);
            if(child instanceof Container) result.addAll(namedFields((Container)child,name));
        }
        return result;
    }

    private static javax.swing.JTextField namedField(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (child instanceof javax.swing.JTextField && name.equals(child.getName())) return (javax.swing.JTextField)child;
            if (child instanceof Container) {
                javax.swing.JTextField found = namedField((Container)child,name);
                if (found != null) return found;
            }
        }
        return null;
    }
    private static List<AbstractButton> editButtons(Container parent) {
        List<AbstractButton> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (child instanceof AbstractButton && "overlay-selector".equals(child.getName())) result.add((AbstractButton)child);
            if (child instanceof Container) result.addAll(editButtons((Container)child));
        }
        return result;
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

    private static JTabbedPane tabs(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JTabbedPane) return (JTabbedPane) child;
            if (child instanceof Container) {
                JTabbedPane found = findTabs((Container) child);
                if (found != null) return found;
            }
        }
        throw new AssertionError("Missing overlay tabs");
    }
    private static JTabbedPane findTabs(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JTabbedPane) return (JTabbedPane) child;
            if (child instanceof Container) {
                JTabbedPane found = findTabs((Container)child);
                if (found != null) return found;
            }
        }
        return null;
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
