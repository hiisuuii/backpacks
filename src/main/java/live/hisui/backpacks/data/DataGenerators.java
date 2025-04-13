package live.hisui.backpacks.data;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();

        generator.addProvider(event.includeServer(), new RecipeGenerator(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new BlockStateGenerator(generator.getPackOutput(), event.getExistingFileHelper()));
    }
}
