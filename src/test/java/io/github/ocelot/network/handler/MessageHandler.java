package io.github.ocelot.network.handler;

import io.github.ocelot.common.valuecontainer.SyncValueContainerMessage;
import io.github.ocelot.network.DisplayScreenMessage;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface MessageHandler
{
    void handleOpenGuiMessage(DisplayScreenMessage msg, Supplier<NetworkEvent.Context> ctx);

    void handleSyncValueContainerMessage(SyncValueContainerMessage msg, Supplier<NetworkEvent.Context> ctx);
}
