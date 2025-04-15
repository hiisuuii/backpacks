package live.hisui.backpacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/* BackpackContainer.java - updated to use DataComponents.CONTAINER */
public class BackpackContainer extends SimpleContainer {

    @Nullable
    private final ItemStack backpackStack;
    private ItemContainerContents sourceContainer;

    public BackpackContainer(int size, ItemStack backpackItem, ItemContainerContents sourceContainer) {
        super(size);
        this.backpackStack = backpackItem;
        this.sourceContainer = sourceContainer;

        // Copy all items from the source container
        for (int i = 0; i < sourceContainer.getSlots(); i++) {
            this.setItem(i, sourceContainer.getStackInSlot(i).copy());
        }
    }

    @Override
    public void startOpen(Player player) {
        Level level = player.level();
        level.playSound(null, BlockPos.containing(player.position()), Backpacks.BACKPACK_OPEN.get(), SoundSource.PLAYERS, 0.6f, 0.7f);
        super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        Level level = player.level();
        level.playSound(null, BlockPos.containing(player.position()), Backpacks.BACKPACK_CLOSE.get(), SoundSource.PLAYERS, 0.6f, 0.5f);
        super.stopOpen(player);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        // Save changes back to the original container
        if (backpackStack != null) {
            // Copy all items back to the source container
            List<ItemStack> contents = new ArrayList<>();
            for (int i = 0; i < this.getContainerSize(); i++) {
                contents.add(this.getItem(i).copy());
            }
            sourceContainer = ItemContainerContents.fromItems(contents);

            // Update the component in the backpack item
            backpackStack.set(DataComponents.CONTAINER, sourceContainer);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return backpackStack != null && player.getInventory().contains(backpackStack);
    }
}