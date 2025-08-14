package live.hisui.backpacks.network;

import io.netty.buffer.ByteBuf;
import live.hisui.backpacks.BackpackContainer;
import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.item.BackpackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;

public record OpenBackpackPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenBackpackPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    Backpacks.MODID, "open_backpack"
            ));

    public static final StreamCodec<ByteBuf, OpenBackpackPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenBackpackPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenBackpackPacket packet, IPayloadContext ctx){
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack backpackStack = Backpacks.findFirstBackpack(player);
            Backpacks.LOGGER.debug("Found backpack: {}", backpackStack);
            if(!backpackStack.isEmpty()) {
                Backpacks.LOGGER.debug("Backpack stack: NOT empty");
                ItemContainerContents container = backpackStack.get(DataComponents.CONTAINER);
                int size = ((BackpackItem) backpackStack.getItem()).getSize();
                if (container == null) {
                    container = ItemContainerContents.fromItems(Collections.nCopies(size, ItemStack.EMPTY));
                    backpackStack.set(DataComponents.CONTAINER, container);
                }

                // Create the wrapper container that will save changes back to the item
                BackpackContainer backpackContainer = new BackpackContainer(size, backpackStack, container);

                player.openMenu(((BackpackItem) backpackStack.getItem()).getMenuProvider(backpackContainer, backpackStack));
            }
        });
    }
}
