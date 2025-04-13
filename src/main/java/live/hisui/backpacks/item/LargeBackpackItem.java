package live.hisui.backpacks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public class LargeBackpackItem extends BackpackItem{
    public LargeBackpackItem(Properties properties, int size) {
        super(properties, size);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return super.canEquip(stack, armorType, entity);
    }

    @Override
    public MenuProvider getMenuProvider(Container container) {
        return new SimpleMenuProvider(((containerId, playerInventory, player) ->
                ChestMenu.sixRows(containerId, playerInventory, container)), Component.translatable("item.backpacks.large_backpack"));
    }
}
