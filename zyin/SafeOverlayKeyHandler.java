package zyin;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

class SafeOverlayKeyHandler extends KeyHandler
{
    private Minecraft mc = Minecraft.getMinecraft();
    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);

    public SafeOverlayKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
    {
        super(keyBindings, repeatings);
    }

    @Override
    public String getLabel()
    {
        return "Safe Overlay Key Handler";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        if (!tickEnd)
            return;	//this fixes an issue with the method being called twice

        if(mc.currentScreen != null)
        	return;	//don't activate if the user is looking at a GUI
        
        //if Control is pressed, enable see through mode
        if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
        	|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
        {
        	SafeOverlay.instance.renderUnsafePositionsThroughWalls = !SafeOverlay.instance.renderUnsafePositionsThroughWalls;	//toggle
        	return;
        }
        
        ZyinMod.SafeOverlayMode++;

        //0=off, 1=on
        if (ZyinMod.SafeOverlayMode > 1)
        {
            ZyinMod.SafeOverlayMode = 0;
        }
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