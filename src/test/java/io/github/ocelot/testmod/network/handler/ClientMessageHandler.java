package io.github.ocelot.testmod.network.handler;

import io.github.ocelot.common.valuecontainer.SyncValueContainerMessage;
import io.github.ocelot.common.valuecontainer.ValueContainer;
import io.github.ocelot.testmod.TestMod;
import io.github.ocelot.testmod.client.screen.TestValueContainerEditorScreen;
import io.github.ocelot.testmod.network.DisplayScreenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientMessageHandler implements MessageHandler
{
    public static final MessageHandler INSTANCE = new ClientMessageHandler();

    private ClientMessageHandler() {}

    @Override
    public void handleOpenGuiMessage(DisplayScreenMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft minecraft = Minecraft.getInstance();
        World world = minecraft.world;

        ctx.get().enqueueWork(() ->
        {
            BlockPos pos = msg.getPos();

            if (world == null)
                return;

            if (msg.getType() == DisplayScreenMessage.GuiType.VALUE_CONTAINER_EDITOR)
            {
                if (pos == null)
                {
                    TestMod.LOGGER.error("Gui packet " + msg.getType() + " was expected to have a block pos!");
                    return;
                }

                if (!(world.getTileEntity(pos) instanceof ValueContainer))
                {
                    TestMod.LOGGER.error("Tile Entity at '" + pos + "' was expected to be a ValueContainer, but it was " + world.getTileEntity(pos) + "!");
                    return;
                }
                minecraft.displayGuiScreen(new TestValueContainerEditorScreen((ValueContainer) Objects.requireNonNull(world.getTileEntity(pos))));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void handleSyncValueContainerMessage(SyncValueContainerMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        throw new UnsupportedOperationException("Client cannot be told to sync Value Containers");
    }
}
