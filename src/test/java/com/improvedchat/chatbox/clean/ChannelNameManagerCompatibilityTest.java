package com.improvedchat.chatbox.clean;

import java.lang.reflect.Field;
import java.util.Arrays;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChannelNameManagerCompatibilityTest {
    @Test public void ticksTrackReadyJoinsAndResyncsWithoutLosingPreviousChannelNames() throws Exception {
        ChannelNameManager names=new ChannelNameManager();Client client=mock(Client.class);
        Field field=ChannelNameManager.class.getDeclaredField("client");field.setAccessible(true);field.set(names,client);
        EventBus bus=new EventBus();bus.register(names);bus.post(new GameTick());assertTrue(names.getFriendsChatNames().isEmpty());
        FriendsChatManager manager=mock(FriendsChatManager.class);when(client.getFriendsChatManager()).thenReturn(manager);
        when(manager.getName()).thenReturn("First");bus.post(new GameTick());bus.post(new GameTick());
        when(manager.getName()).thenReturn("Second");bus.post(new GameTick());
        when(client.getFriendsChatManager()).thenReturn(null);bus.post(new GameTick());
        assertEquals(Arrays.asList("First","Second"),names.getFriendsChatNames());
        bus.unregister(names);names.shutdown();when(client.getFriendsChatManager()).thenReturn(manager);bus.post(new GameTick());assertTrue(names.getFriendsChatNames().isEmpty());
        bus.register(names);bus.post(new GameTick());assertEquals(Arrays.asList("Second"),names.getFriendsChatNames());bus.unregister(names);
    }
}
