package live.hisui.backpacks;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import live.hisui.backpacks.block.BackpackBlock;
import live.hisui.backpacks.block.entity.BackpackBlockEntity;
import live.hisui.backpacks.block.entity.LargeBackpackBlockEntity;
import live.hisui.backpacks.compat.curios.CuriosCompat;
import live.hisui.backpacks.data.DataGenerators;
import live.hisui.backpacks.item.BackpackItem;
import live.hisui.backpacks.item.LargeBackpackItem;
import live.hisui.backpacks.network.OpenBackpackPacket;
import live.hisui.backpacks.recipe.BackpackUpgradeRecipe;
import live.hisui.backpacks.render.BackpackRenderLayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Backpacks.MODID)
public class Backpacks
{
    public static final String MODID = "backpacks";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BACKPACK_OPEN = SOUND_EVENTS.register(
            "backpacks.backpack.open", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "backpacks.backpack.open"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BACKPACK_CLOSE = SOUND_EVENTS.register(
            "backpacks.backpack.close", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "backpacks.backpack.close"))
    );

    public static final DeferredItem<Item> BACKPACK = registerItem("backpack",
            () -> new BackpackItem(new Item.Properties()
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY), 27));
    public static final DeferredItem<Item> LARGE_BACKPACK = registerItem("large_backpack",
            () -> new LargeBackpackItem(new Item.Properties()
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY), 54));

    public static final DeferredBlock<Block> BACKPACK_BLOCK = BLOCKS.register("backpack", () ->
            new BackpackBlock(BlockBehaviour.Properties.of().strength(0.8f).mapColor(MapColor.COLOR_BROWN)
                    .sound(SoundType.SNOW)));
    public static final DeferredBlock<Block> LARGE_BACKPACK_BLOCK = BLOCKS.register("large_backpack", () ->
            new BackpackBlock(BlockBehaviour.Properties.of().strength(0.8f).mapColor(MapColor.COLOR_BROWN)
                    .sound(SoundType.SNOW)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BackpackBlockEntity>> BACKPACK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "backpack",() -> BlockEntityType.Builder.of(BackpackBlockEntity::new, Backpacks.BACKPACK_BLOCK.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeBackpackBlockEntity>> LARGE_BACKPACK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "large_backpack",() -> BlockEntityType.Builder.of(LargeBackpackBlockEntity::new,
                    Backpacks.LARGE_BACKPACK_BLOCK.get()).build(null)
    );

    private static DeferredItem<Item> registerItem(String name, Supplier<Item> supp) {
        return ITEMS.register(name, supp);
    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> BACKPACK_UPGRADE_RECIPE = RECIPES.register(
            "backpack_upgrade_recipe", BackpackUpgradeRecipe.Serializer::new
    );

    public Backpacks(IEventBus modEventBus, ModContainer modContainer)
    {

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RECIPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        if(ModList.get().isLoaded("curios")){
            modEventBus.addListener(CuriosCompat::registerCapabilities);
        }

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Backpacks) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
    }

    public static ItemStack findChestBackpack(Player player){
        ItemStack backpackStack = ItemStack.EMPTY;
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if(chestStack.getItem() instanceof BackpackItem){
            backpackStack = chestStack;
        }
        return backpackStack;
    }

    public static ItemStack findFirstBackpack(Player player){
        ItemStack backpackStack = ItemStack.EMPTY;
        if(ModList.get().isLoaded("curios")){
            backpackStack = CuriosCompat.findCurioBackpack(player);
        }
        if(backpackStack.isEmpty()) {
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack.isEmpty()) {
                for (ItemStack stack : player.getInventory().items.reversed()) {
                    if (stack.getItem() instanceof BackpackItem) {
                        backpackStack = stack;
                    }
                }
            } else {
                backpackStack = chestStack;
            }
        }

        return backpackStack;
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class CommonEvents {
        @SubscribeEvent
        public static void registerPayloads(final RegisterPayloadHandlersEvent event){
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(OpenBackpackPacket.TYPE, OpenBackpackPacket.STREAM_CODEC,
                    OpenBackpackPacket::handle);
        }
    }



    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        public static final Lazy<KeyMapping> OPEN_BACKPACK = Lazy.of(() -> new KeyMapping(
                "key.backpacks.open_backpack",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.inventory"
        ));

        @SubscribeEvent
        public static void registerRenderLayers(EntityRenderersEvent.AddLayers event){
            // Add to player renderers
            addLayerToPlayerRenderer(event.getSkin(PlayerSkin.Model.WIDE));
            addLayerToPlayerRenderer(event.getSkin(PlayerSkin.Model.SLIM));

            // Add to other humanoid entities if needed
            // Example for zombies, skeletons, etc.
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
                EntityRenderer<?> entityRenderer = event.getRenderer(entityType);
                if(entityRenderer instanceof LivingEntityRenderer<?,?> renderer){
                    if (renderer.getModel() instanceof HumanoidModel<?>) {
                        @SuppressWarnings("unchecked")
                        LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>> livingRenderer =
                                (LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>>) renderer;
                        livingRenderer.addLayer(new BackpackRenderLayer<>(livingRenderer));
                    }
                }
            }
        }

        private static void addLayerToPlayerRenderer(PlayerRenderer renderer) {
            if (renderer != null) {
                renderer.addLayer(new BackpackRenderLayer<>(renderer));
            }
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            if(ModList.get().isLoaded("curios")){
                CuriosCompat.registerRenderer(event);
            }
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event){
            event.register(OPEN_BACKPACK.get());
        }



    }
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event){
            while (ClientModEvents.OPEN_BACKPACK.get().consumeClick()){
                PacketDistributor.sendToServer(new OpenBackpackPacket());
            }
        }

    }

}
