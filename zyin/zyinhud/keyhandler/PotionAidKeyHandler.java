package zyin.zyinhud.keyhandler;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import zyin.zyinhud.PotionAid;
import zyin.zyinhud.ZyinHUD;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class PotionAidKeyHandler extends KeyHandler
{
    private Minecraft mc = Minecraft.getMinecraft();
    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);


    public PotionAidKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
    {
        super(keyBindings, repeatings);
    }

    @Override
    public String getLabel()
    {
        return "Potion Aid Key Handler";
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

        if (ZyinHUD.EnablePotionAid)
        {
            PotionAid.instance.Drink();
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