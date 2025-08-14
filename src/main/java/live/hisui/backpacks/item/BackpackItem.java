package live.hisui.backpacks.item;

import live.hisui.backpacks.BackpackContainer;
import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.block.BackpackBlock;
import live.hisui.backpacks.block.entity.BackpackBlockEntity;
import live.hisui.backpacks.block.entity.LargeBackpackBlockEntity;
import live.hisui.backpacks.compat.curios.CuriosCompat;
import live.hisui.backpacks.menu.BackpackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import java.util.Collections;
import java.util.Optional;

/* BackpackItem.java - updated to use DataComponents.CONTAINER */
public class BackpackItem extends Item implements Equipable {
    private final int size;
    public BackpackItem(Properties properties, int size) {
        super(properties.stacksTo(1));
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        if(entity instanceof Player player){
            if(ModList.get().isLoaded("curios")){
                if(!CuriosCompat.findCurioBackpack(player).isEmpty()){
                    return false;
                }
            }
        }
        return super.canEquip(stack, armorType, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null || !player.isShiftKeyDown()){
            return InteractionResult.PASS;
        }

        Direction direction = player.getDirection().getOpposite();

        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        InteractionResult interactionResult = tryPlace(player, direction, blockPlaceContext);
        return interactionResult == InteractionResult.PASS ? super.useOn(context) : interactionResult;
    }

    public InteractionResult tryPlace(Player player, Direction direction, BlockPlaceContext context){
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState placementState = context.getItemInHand().is(Backpacks.BACKPACK.get()) ? Backpacks.BACKPACK_BLOCK.get().defaultBlockState().setValue(BackpackBlock.HORIZONTAL_FACING, direction) : Backpacks.LARGE_BACKPACK_BLOCK.get().defaultBlockState().setValue(BackpackBlock.HORIZONTAL_FACING, direction);
        if(level.setBlockAndUpdate(pos, placementState)){
            ItemStack backpack = context.getItemInHand();
            BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) level.getBlockEntity(pos);
            if(backpackBlockEntity != null) {
                if(backpackBlockEntity instanceof LargeBackpackBlockEntity lbbe){
                    lbbe.setItemsFromContents(Optional.ofNullable(backpack.get(DataComponents.CONTAINER)).orElse(ItemContainerContents.fromItems(Collections.nCopies(54, ItemStack.EMPTY))));
                } else {
                    backpackBlockEntity.setItemsFromContents(Optional.ofNullable(backpack.get(DataComponents.CONTAINER)).orElse(ItemContainerContents.fromItems(Collections.nCopies(27, ItemStack.EMPTY))));
                }
            }
            level.playSound(player,pos, SoundEvents.SNOW_PLACE, SoundSource.BLOCKS, 1.0f, 0.6f);
            if(player == null || !player.isCreative()){
                backpack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!level.isClientSide()) {
            ItemStack usedStack = player.getItemInHand(usedHand);

            // Get the container component or create it if it doesn't exist
            ItemContainerContents container = usedStack.get(DataComponents.CONTAINER);
            if (container == null) {
                container = ItemContainerContents.fromItems(Collections.nCopies(size, ItemStack.EMPTY));
                usedStack.set(DataComponents.CONTAINER, container);
            }

            // Create the wrapper container that will save changes back to the item
            BackpackContainer backpackContainer = new BackpackContainer(size, usedStack, container);

            // Open the menu
            player.openMenu(getMenuProvider(backpackContainer, usedStack));
        }
//        level.playSound(player, BlockPos.containing(player.position()), SoundEvents.LLAMA_CHEST, SoundSource.PLAYERS, 1.0f, 0.8f);

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    public MenuProvider getMenuProvider(Container container, ItemStack self){
        return new SimpleMenuProvider(((containerId, playerInventory, player) ->
                BackpackMenu.smallBackpack(containerId, playerInventory, container, self)), Component.translatable("item.backpacks.backpack"));
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }
}
