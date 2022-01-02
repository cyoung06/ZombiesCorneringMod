package kr.syeyoung.corneringmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding toggleCornering;

    public static KeyBinding toggleBlockAlarm;
    public static void register()
    {
        toggleCornering = new KeyBinding("key.toggleCornering", Keyboard.KEY_P, "key.categories.zhf");
        toggleBlockAlarm = new KeyBinding("key.toggleBlockAlarm", Keyboard.KEY_P, "key.categories.zhf");
 
        ClientRegistry.registerKeyBinding(toggleCornering);
        ClientRegistry.registerKeyBinding(toggleBlockAlarm);
    }
}