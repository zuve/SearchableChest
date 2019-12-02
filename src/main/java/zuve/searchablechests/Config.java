package zuve.searchablechests;

import java.util.ArrayList;

import net.minecraftforge.common.ForgeConfigSpec;

final class Config {
	final ForgeConfigSpec.ConfigValue<Boolean> autoFocus;
	final ForgeConfigSpec.ConfigValue<Integer> minimumContainerSize;
	final ForgeConfigSpec.ConfigValue<ArrayList<String>> blacklist;

	Config(final ForgeConfigSpec.Builder builder) {
		autoFocus = builder.comment("Whether the search bar will be focused by default when opening containers")
				.translation("searchablechests.config.autoFocus").define("autoFocus", false);
		minimumContainerSize = builder.comment("Minimum size a container must be for a search bar to be added").translation("searchablechests.config.minimumContainerSize").defineInRange("minimumContainerSize", 27, 0, Integer.MAX_VALUE);
		blacklist = builder.comment(
				"Comma separated list of containers in which the search bar should be disabled. Name should be that which appears in the top left of the container's GUI. Example: blacklist=[\"Chest\", \"Large Chest\"]")
				.translation("searchablechests.config.blacklist").define("blacklist", new ArrayList<String>());
	}
}
