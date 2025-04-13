package live.hisui.backpacks.compat.curios;

import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.item.BackpackItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CuriosCompat {

    public static void registerCapabilities(final RegisterCapabilitiesEvent event){
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }

                    @Override
                    public boolean canEquip(SlotContext context1){
                        Entity entity = context1.entity();
                        if(entity instanceof Player player){
                            return Backpacks.findChestBackpack(player).isEmpty();
                        }
                        return true;
                    }
                }, Backpacks.BACKPACK, Backpacks.LARGE_BACKPACK
        );
    }

    public static boolean canEquipBackpack(Player player, ItemStack stack){
        return Optional.ofNullable(stack.getCapability(CuriosCapability.ITEM)).map(cap -> cap.canEquip(new SlotContext("back", player, 0, false, true))).orElse(false);
    }

    public static ItemStack findCurioBackpack(Player player){
        ItemStack backpackStack = ItemStack.EMPTY;
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        Optional<SlotResult> backpackSlot = curiosInventory.flatMap(inv -> inv.findFirstCurio((stack) -> stack.getItem() instanceof BackpackItem));
        if(backpackSlot.isPresent()){
            backpackStack = backpackSlot.get().stack();
        }
        return backpackStack;
    }

    public static boolean hasOpenBackSlot(Player player){
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        Optional<ICurioStacksHandler> backSlot = curiosInventory.flatMap(inv -> inv.getStacksHandler("back"));
        AtomicBoolean hasRoom = new AtomicBoolean(false);
        backSlot.ifPresent(handler -> {
            for(int i = 0; i < handler.getSlots(); i++){
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if(stack.isEmpty()){
                    hasRoom.set(true);
                }
            }
        });
        return hasRoom.get();
    }

    public static boolean insertIntoBackSlot(Player player, ItemStack itemStack){
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        Optional<ICurioStacksHandler> backSlot = curiosInventory.flatMap(inv -> inv.getStacksHandler("back"));
        AtomicBoolean success = new AtomicBoolean(false);
        backSlot.ifPresent(handler -> {
            for(int i = 0; i < handler.getSlots(); i++){
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if(stack.isEmpty()){
                    handler.getStacks().setStackInSlot(i, itemStack.copy());
                    itemStack.shrink(1);
                    success.set(true);
                }
            }
        });
        return success.get();
    }

    public static void registerRenderer(final FMLClientSetupEvent event){
        CuriosRendererRegistry.register(Backpacks.BACKPACK.get(), BackpackCurioRenderer::new);
        CuriosRendererRegistry.register(Backpacks.LARGE_BACKPACK.get(), BackpackCurioRenderer::new);
    }
}
