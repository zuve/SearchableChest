package zuve.searchablechests;

import java.io.File;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "searchablechests", name="Searchable Chests", version = "1.12-1.5.3", acceptedMinecraftVersions="[1.12]")
@SideOnly(value = Side.CLIENT)
public class SearchableChests {
	
	public static Configuration config;
	
    public static Logger logger;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        File directory = event.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "searchablechests.cfg"));
        Config.readConfig();
    }

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ChestEventHandler());
	}
	
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    	if (config.hasChanged()) {
            config.save();
        }
    }
}
