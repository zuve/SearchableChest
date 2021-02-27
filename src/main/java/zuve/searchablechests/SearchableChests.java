package zuve.searchablechests;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("searchablechests")
public class SearchableChests {
	
	private static final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
	public static final ForgeConfigSpec CONFIG_SPEC = specPair.getRight();
	public static final Config CONFIG = specPair.getLeft();
	
	public SearchableChests() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new SearchableChestsRunnable());
	}

	class SearchableChestsRunnable implements DistExecutor.SafeRunnable {
		private static final long serialVersionUID = 7343468567232854267L;

		@Override
		public void run() {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SearchableChests.CONFIG_SPEC);
			MinecraftForge.EVENT_BUS.register(new ChestEventHandler());
		}
	}
}
