package io.github.ocelot.network.handler;

import io.github.ocelot.common.valuecontainer.SyncValueContainerMessage;
import io.github.ocelot.common.valuecontainer.ValueContainer;
import io.github.ocelot.TestMod;
import io.github.ocelot.network.DisplayScreenMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerMessageHandler implements MessageHandler
{
    public static final MessageHandler INSTANCE = new ServerMessageHandler();

    private ServerMessageHandler() {}

    @Override
    public void handleOpenGuiMessage(DisplayScreenMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        throw new UnsupportedOperationException("Server cannot open a GUI");
    }

    @Override
    public void handleSyncValueContainerMessage(SyncValueContainerMessage msg, Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();

        ctx.get().enqueueWork(() ->
        {
            if (player == null)
                return;
            World world = player.world;
            BlockPos pos = msg.getPos();

            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof ValueContainer) && !(world.getBlockState(pos).getBlock() instanceof ValueContainer))
            {
                TestMod.LOGGER.error("Tile Entity or Block at '" + pos + "' was expected to be a ValueContainer, but it was " + te + "!");
                return;
            }

            if (!player.canUseCommandBlock())
            {
                TestMod.LOGGER.error("Player with id " + player.getUniqueID() + " does not have the permission to modify value containers!");
                return;
            }

            ValueContainer.deserialize(world, pos, te instanceof ValueContainer ? (ValueContainer) te : (ValueContainer) world.getBlockState(pos).getBlock(), msg.getData());
        });
        ctx.get().setPacketHandled(true);
    }
}
