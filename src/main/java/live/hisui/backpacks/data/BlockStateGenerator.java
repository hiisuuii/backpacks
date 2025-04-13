package live.hisui.backpacks.data;

import live.hisui.backpacks.Backpacks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Backpacks.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Block block = Backpacks.BACKPACK_BLOCK.get();
        horizontalBlock(block, models().getExistingFile(ModelLocationUtils.getModelLocation(block)));
        block = Backpacks.LARGE_BACKPACK_BLOCK.get();
        horizontalBlock(block, models().getExistingFile(ModelLocationUtils.getModelLocation(block)));
    }


}
