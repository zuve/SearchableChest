package zuve.searchablechests;

import org.apache.logging.log4j.Level;

import net.minecraftforge.common.config.Configuration;

final class Config {
	public static final String GENERAL = "general";

	public static boolean autoFocus = false;
	public static boolean autoSelect = true;
	public static int minimumContainerSize = 27;

	public static void readConfig() {
		Configuration cfg = SearchableChests.config;
		try {
			cfg.load();
			autoFocus = cfg.getBoolean("autoFocus", GENERAL, autoFocus,
					"Whether the search bar will be focused by default when opening containers");
			autoSelect = cfg.getBoolean("autoSelect", GENERAL, autoSelect,
					"Whether the contents of the search bar will be selected when it is focused");
			minimumContainerSize = cfg.getInt("minimumContainerSize", GENERAL, minimumContainerSize, 0,
					Integer.MAX_VALUE, "Minimum size a container must be for a search bar to be added");
		} catch (Exception e1) {
			SearchableChests.logger.log(Level.ERROR, "Problem loading config file for Searchable Chests", e1);
		} finally {
			if (cfg.hasChanged()) {
				cfg.save();
			}
		}
	}
}
