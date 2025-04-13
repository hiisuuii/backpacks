package live.hisui.backpacks.recipe;

import com.mojang.serialization.MapCodec;
import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.item.BackpackItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BackpackUpgradeRecipe extends ShapedRecipe {
    public BackpackUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        super(group, category, pattern, result);
    }

    public BackpackUpgradeRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.pattern, shapedRecipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        Optional<ItemStack> inputPack = getBackpack(input);
        if(inputPack.isPresent()){
            ItemStack backpackToUpgrade = inputPack.get();
            BackpackItem backpackType = (BackpackItem) backpackToUpgrade.getItem();
            ItemContainerContents contentsSmall = backpackToUpgrade.get(DataComponents.CONTAINER);
            if(contentsSmall != null) {

                ItemStack[] stacks = new ItemStack[backpackType.getSize()];
                for (int i = 0; i < contentsSmall.getSlots(); i++) {
                    stacks[i] = contentsSmall.getStackInSlot(i);
                }
                List<ItemStack> stacksLarge = new ArrayList<>(Collections.nCopies(((BackpackItem)result.getItem()).getSize(), ItemStack.EMPTY));
                for (int i = 0; i < stacks.length; i++) {
                    stacksLarge.set(i, stacks[i]);
                }
                ItemContainerContents contentsLarge = ItemContainerContents.fromItems(stacksLarge);


                result.set(DataComponents.CONTAINER, contentsLarge);
            }
        }


        return result;
    }

    private Optional<ItemStack> getBackpack(CraftingInput inv) {
        for(ItemStack stack : inv.items()){
            if(stack.getItem() instanceof BackpackItem){
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Backpacks.BACKPACK_UPGRADE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<BackpackUpgradeRecipe>{
        private static final MapCodec<BackpackUpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(BackpackUpgradeRecipe::new, o -> o);
        private static final StreamCodec<RegistryFriendlyByteBuf, BackpackUpgradeRecipe> STREAM_CODEC = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(BackpackUpgradeRecipe::new, BackpackUpgradeRecipe::new);

        @Override
        public MapCodec<BackpackUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BackpackUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
