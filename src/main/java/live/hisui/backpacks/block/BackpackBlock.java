package live.hisui.backpacks.block;

import com.mojang.serialization.MapCodec;
import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.block.entity.BackpackBlockEntity;
import live.hisui.backpacks.block.entity.LargeBackpackBlockEntity;
import live.hisui.backpacks.compat.curios.CuriosCompat;
import live.hisui.backpacks.item.BackpackItem;
import live.hisui.backpacks.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

public class BackpackBlock extends BaseEntityBlock {
    public static final MapCodec<BackpackBlock> CODEC = simpleCodec(BackpackBlock::new);
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SMALL_SHAPE_NORTH = makeSmallShape();
    private static final VoxelShape SMALL_SHAPE_SOUTH = VoxelShapeUtils.rotateHorizontal(makeSmallShape(), Direction.SOUTH);
    private static final VoxelShape SMALL_SHAPE_EAST = VoxelShapeUtils.rotateHorizontal(makeSmallShape(), Direction.EAST);
    private static final VoxelShape SMALL_SHAPE_WEST = VoxelShapeUtils.rotateHorizontal(makeSmallShape(), Direction.WEST);
    private static final VoxelShape LARGE_SHAPE_NORTH = makeLargeShape();
    private static final VoxelShape LARGE_SHAPE_SOUTH = VoxelShapeUtils.rotateHorizontal(makeLargeShape(), Direction.SOUTH);
    private static final VoxelShape LARGE_SHAPE_EAST = VoxelShapeUtils.rotateHorizontal(makeLargeShape(), Direction.EAST);
    private static final VoxelShape LARGE_SHAPE_WEST = VoxelShapeUtils.rotateHorizontal(makeLargeShape(), Direction.WEST);

    private static VoxelShape makeSmallShape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.3125, 0.75, 0.375, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.4375, 0.8125, 0.6875, 0.6875), BooleanOp.OR);

        return shape;
    }
    private static VoxelShape makeLargeShape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.3125, 0.75, 0.5625, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.4375, 0.8125, 0.6875, 0.6875), BooleanOp.OR);

        return shape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(direction){
            case EAST -> {
                return state.is(Backpacks.BACKPACK_BLOCK.get()) ? SMALL_SHAPE_EAST : LARGE_SHAPE_EAST;
            }
            case WEST -> {
                return state.is(Backpacks.BACKPACK_BLOCK.get()) ? SMALL_SHAPE_WEST : LARGE_SHAPE_WEST;
            }
            case SOUTH -> {
                return state.is(Backpacks.BACKPACK_BLOCK.get()) ? SMALL_SHAPE_SOUTH : LARGE_SHAPE_SOUTH;
            }
            default -> {
                return state.is(Backpacks.BACKPACK_BLOCK.get()) ? SMALL_SHAPE_NORTH : LARGE_SHAPE_NORTH;
            }
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public MapCodec<BackpackBlock> codec() {
        return CODEC;
    }

    public BackpackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if(player.isShiftKeyDown()){
                level.playSound(player, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
                ItemStack stack = this.getDrops(state, new LootParams.Builder((ServerLevel) level)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .withParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos))).getFirst();
                if(ModList.get().isLoaded("curios")){
                    if(CuriosCompat.hasOpenBackSlot(player) && CuriosCompat.canEquipBackpack(player, stack)){
                        if(CuriosCompat.insertIntoBackSlot(player, stack)){
                            level.destroyBlock(pos, false);
                            return InteractionResult.SUCCESS;
                        } else {
                            return InteractionResult.PASS;
                        }
                    }
                }
                if(player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && stack.getItem().canEquip(stack, EquipmentSlot.CHEST, player)){
                    player.setItemSlot(EquipmentSlot.CHEST, stack.copy());
                    stack.shrink(1);
                    level.destroyBlock(pos, false);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            } else {
                if (blockentity instanceof BackpackBlockEntity) {
                    level.playSound(player, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1.0f, 0.8f);
                    player.openMenu((BackpackBlockEntity) blockentity);
                }
            }

            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.is(Backpacks.LARGE_BACKPACK_BLOCK)) return new LargeBackpackBlockEntity(pos, state);
        return new BackpackBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Returns the analog signal this block emits. This is the signal a comparator can read from it.
     *
     */
    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getNearestLookingDirection().getOpposite());
    }
}
