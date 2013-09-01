package zyin;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class HUDTickHandler implements ITickHandler
{

    private static Minecraft mc = Minecraft.getMinecraft();
    
    public HUDTickHandler()
    {
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return "HUD Tick Handler";
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.RENDER)))
        {
            onRenderTick();
        }
        else if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
            GuiScreen guiScreen = HUDTickHandler.mc.currentScreen;

            if (guiScreen == null)
            {
                onTickInGame();
            }
            else
            {
                onTickInGUI(guiScreen);
            }
        }
    }

    protected void onTickInGUI(GuiScreen guiScreen)
    {
    	
    }
    
    /**
     * Render any things that need to be rendered onto the user's HUD (on the screen, NOT in the game
     * world - that is done in the renderWorldLastEvent() method in ZyinMod.java)
     */
    protected void onRenderTick()
    {
        InfoLine.Render();
        DurabilityInfo.Render();
        PotionTimers.Render();
        PlayerLocator.Render();
    }

    protected void onTickInGame()
    {
    	
    }

    
}