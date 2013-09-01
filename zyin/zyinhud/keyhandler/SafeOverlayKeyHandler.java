package zyin.zyinhud.keyhandler;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import zyin.zyinhud.InfoLine;
import zyin.zyinhud.SafeOverlay;
import zyin.zyinhud.ZyinHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.Property;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class SafeOverlayKeyHandler extends KeyHandler
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
        {
            return;    //this fixes an issue with the method being called twice
        }

        if (mc.currentScreen != null)
        {
            return;    //don't activate if the user is looking at a GUI
        }
        

        //if Control is pressed, enable see through mode
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
        {
        	boolean seeThroughWalls = SafeOverlay.instance.toggleSeeUnsafePositionsThroughWalls();
        	
        	if(seeThroughWalls)
            	InfoLine.DisplayNotification("See through walls Enabled");
            else
            	InfoLine.DisplayNotification("See through walls Disabled");
            
            SafeOverlay.instance.RecalculateUnsafePositions();
        	
            return;
        }
        
        
        //if "+" is pressed, increase the draw distance
        if (Keyboard.isKeyDown(Keyboard.KEY_EQUALS) || 	//keyboard "+" ("=")
        		Keyboard.isKeyDown(Keyboard.KEY_ADD))	//numpad "+"
        {
            int drawDistance = SafeOverlay.instance.increaseDrawDistance();
            
            if(drawDistance == SafeOverlay.maxDrawDistance)
            	InfoLine.DisplayNotification("Safe Overlay distance: "+drawDistance + " (max)");
            else
            	InfoLine.DisplayNotification("Safe Overlay distance: "+drawDistance);
            
            SafeOverlay.instance.RecalculateUnsafePositions();
            
            return;
        }
        
        
        //if "-" is pressed, decrease the draw distance
        if (Keyboard.isKeyDown(Keyboard.KEY_MINUS))
        {
            int drawDistance = SafeOverlay.instance.decreaseDrawDistance();
            
        	InfoLine.DisplayNotification("Safe Overlay distance: "+drawDistance);
            
            SafeOverlay.instance.RecalculateUnsafePositions();
            
            return;
        }
        
        
        //if "0" is pressed, set to the default draw distance
        if (Keyboard.isKeyDown(Keyboard.KEY_0))
        {
            int drawDistance = SafeOverlay.instance.setDrawDistance(SafeOverlay.defaultDrawDistance);
        	SafeOverlay.instance.setSeeUnsafePositionsThroughWalls(false);
            
        	InfoLine.DisplayNotification("Safe Overlay settings: default ("+drawDistance+")");
            
            SafeOverlay.instance.RecalculateUnsafePositions();
            
            return;
        }
        
        
        ZyinHUD.SafeOverlayMode++;

        //0=off, 1=on
        if (ZyinHUD.SafeOverlayMode > 1)
        {
            ZyinHUD.SafeOverlayMode = 0;
        }
        
        if(ZyinHUD.SafeOverlayMode == 1)
        {
        	//if we enable the mod, calculate unsafe areas immediately
            SafeOverlay.instance.RecalculateUnsafePositions();
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