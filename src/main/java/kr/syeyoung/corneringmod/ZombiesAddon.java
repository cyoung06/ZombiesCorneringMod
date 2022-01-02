package kr.syeyoung.corneringmod;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ZombiesAddon.MODID, version = ZombiesAddon.VERSION)
public class ZombiesAddon
{
    public static final String MODID = "zombies_cornering_mod";
    public static final String VERSION = "2.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Keybinds.register();
        FMLCommonHandler.instance().bus().register(new EventListener());
    }
}
