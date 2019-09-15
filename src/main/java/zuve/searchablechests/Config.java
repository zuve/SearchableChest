package zuve.searchablechests;

import net.minecraftforge.common.ForgeConfigSpec;

final class Config {
	final ForgeConfigSpec.ConfigValue<Boolean> autoFocus;
	final ForgeConfigSpec.ConfigValue<Integer> minimumContainerSize;

	Config(final ForgeConfigSpec.Builder builder) {
		autoFocus = builder.comment("Whether the search bar will be focused by default when opening containers")
				.translation("searchablechests.config.autoFocus").define("autoFocus", false);
		minimumContainerSize = builder.comment("Minimum size a container must be for a search bar to be added").translation("searchablechests.config.minimumContainerSize").defineInRange("minimumContainerSize", 27, 0, Integer.MAX_VALUE);
	}
}
