package live.hisui.backpacks;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BackpacksConfig {
    public static class Common {
        public final ModConfigSpec.BooleanValue backpackNesting;
        Common(ModConfigSpec.Builder builder){
            backpackNesting = builder.comment("Allow backpacks to be nested inside other backpacks:")
                    .define("backpackNesting", true);
        }
    }

    public static final ModConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }
}
