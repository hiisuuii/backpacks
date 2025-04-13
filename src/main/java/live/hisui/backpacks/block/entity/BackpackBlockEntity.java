package live.hisui.backpacks.block.entity;

import live.hisui.backpacks.Backpacks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BackpackBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items;
    public BackpackBlockEntity(BlockPos pos, BlockState blockState) {
        super(Backpacks.BACKPACK_BLOCK_ENTITY.get(), pos, blockState);
        items = NonNullList.withSize(27, ItemStack.EMPTY);
    }
    public BackpackBlockEntity(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        items = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("item.backpacks.backpack");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public void setItemsFromContents(ItemContainerContents contents){
        for(int i = 0; i < contents.getSlots(); i++){
            this.getItems().set(i, contents.getStackInSlot(i));
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }
}
