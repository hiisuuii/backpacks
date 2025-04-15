package live.hisui.backpacks.data;

import live.hisui.backpacks.recipe.BackpackUpgradeRecipe;
import live.hisui.backpacks.Backpacks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Backpacks.LARGE_BACKPACK.get())
                .pattern("LGL")
                .pattern("LBL")
                .pattern("LLL")
                .define('L', Tags.Items.LEATHERS)
                .define('G',Tags.Items.INGOTS_GOLD)
                .define('B',Backpacks.BACKPACK)
                .unlockedBy("has_backpack",has(Backpacks.BACKPACK))
                .save(backpackUpgrade(recipeOutput));
    }

    private static BackpackRecipeOutput<ShapedRecipe> backpackUpgrade(RecipeOutput consumer){
        return new BackpackRecipeOutput<>(consumer, BackpackUpgradeRecipe::new);
    }

    private static class BackpackRecipeOutput<T extends Recipe<?>> implements RecipeOutput {
        private final RecipeOutput output;
        private final Function<T, ? extends T> ctor;
        public BackpackRecipeOutput(RecipeOutput ouput, Function<T, ? extends T> ctorIn){
            this.output = ouput;
            this.ctor = ctorIn;
        }

        @Override
        public Advancement.Builder advancement() {
            return output.advancement();
        }

        @Override
        public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, ICondition... conditions) {
            output.accept(id, ctor.apply((T)recipe), advancementHolder, conditions);
        }
    }


}
