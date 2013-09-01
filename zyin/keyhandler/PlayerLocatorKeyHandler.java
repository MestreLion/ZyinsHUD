package zyin.keyhandler;

import java.util.EnumSet;

import zyin.InfoLine;
import zyin.ZyinHUD;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class PlayerLocatorKeyHandler extends KeyHandler
{
    private Minecraft mc = Minecraft.getMinecraft();
    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);

    public PlayerLocatorKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
    {
        super(keyBindings, repeatings);
    }

    @Override
    public String getLabel()
    {
        return "Player Locator Key Handler";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        if (!tickEnd)
        {
            return;    //this fixes an issue with the method being called twice
        }

        if (mc.currentScreen != null)
        {
            return;    //don't activate if the user is looking at a GUI
        }
        
        
        ZyinHUD.PlayerLocatorMode++;
        
        //0=off, 1=on
        if (ZyinHUD.PlayerLocatorMode > 1)
        {
            ZyinHUD.PlayerLocatorMode = 0;
        }

        /*if(ZyinHUD.PlayerLocatorMode == 0)
        	InfoLine.DisplayNotification("Player Locator: disabled");
        else if(ZyinHUD.PlayerLocatorMode == 1)
        	InfoLine.DisplayNotification("Player Locator: enabled");*/
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        if (!tickEnd)
        {
            return;    //this fixes an issue with the method being called twice
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return tickTypes;
    }
}