package live.hisui.backpacks.menu;

import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.BackpacksConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/*
Extend the ChestMenu to be unable to shift-click the backpack into itself. Should fix the "backpack ouroboros" issue.
 */
public class BackpackMenu extends ChestMenu {
    private final ItemStack self;

    private BackpackMenu(MenuType<?> type, int containerId, Inventory playerInventory, int rows, Container container, ItemStack self) {
        super(type, containerId, playerInventory, container, rows);
        this.self = self;
    }

    public static BackpackMenu smallBackpack(int containerId, Inventory playerInventory, Container container, ItemStack self) {
        return new BackpackMenu(MenuType.GENERIC_9x3, containerId, playerInventory, 3, container, self);
    }

    public static BackpackMenu largeBackpack(int containerId, Inventory playerInventory, Container container, ItemStack self) {
        return new BackpackMenu(MenuType.GENERIC_9x6, containerId, playerInventory, 6, container, self);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if(!BackpacksConfig.COMMON.backpackNesting.get() && (stack.is(Backpacks.BACKPACK) || stack.is(Backpacks.LARGE_BACKPACK))) {
            return false;
        }
        if(ItemStack.isSameItemSameComponents(stack, self)) {
            return false;
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }
}
