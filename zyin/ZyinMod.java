package zyin;

import java.io.File;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ZyinMod", name = "Zyin's HUD", version = "0.3.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ZyinMod
{
    //Configurable values
    public static Boolean ShowCoordinates;
    public static Boolean ShowArmorDurability;
    public static Boolean ShowItemDurability;
    public static int UpdateFrequency;
    public static double DurabilityDisplayThresholdForArmor;
    public static double DurabilityDisplayThresholdForItem;
    public static int DurabilityLocationHorizontal;
    public static int DurabilityLocationVertical;

    //Non-Configurable values
    public static int DistanceMeasurerMode = 0;	//0=off, 1=simple, 2=complex

    @Instance("ZyinMod")
    public static ZyinMod instance;

    @SidedProxy(clientSide = "zyin.ClientProxy", serverSide = "zyin.CommonProxy")
    public static CommonProxy proxy;

    public ZyinMod()
    {
    }

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        LoadConfigSettings(event.getSuggestedConfigurationFile());
    }

    @Init
    public void load(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        //Tick (Render) Handlers
        TickRegistry.registerTickHandler(new HUDTickHandler(), Side.CLIENT);
        //Key Bind Handlers
        KeyBinding[] key = {new KeyBinding("Distance Measurer Toggle", Keyboard.KEY_K)};
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new DistanceMeasurerKeyHandler(key, repeat));
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    private void LoadConfigSettings(File configFile)
    {
        //load configuration settings
        Configuration config = new Configuration(configFile);
        config.load();
        //Categories:
        //config.CATEGORY_GENERAL
        //config.CATEGORY_ITEM
        //config.CATEGORY_BLOCK
        Property p;
        ShowCoordinates = config.get("display", "ShowCoordinates", true).getBoolean(true);
        ShowArmorDurability = config.get("display", "ShowArmorDurability", true).getBoolean(true);
        ShowItemDurability = config.get("display", "ShowItemDurability", true).getBoolean(true);
        p = config.get(Configuration.CATEGORY_GENERAL, "DurabilityDisplayThresholdForArmor", 0.9);
        p.comment = "Display when armor gets damaged more than this fraction of its durability";
        DurabilityDisplayThresholdForArmor = p.getDouble(0.9);
        p = config.get(Configuration.CATEGORY_GENERAL, "DurabilityDisplayThresholdForItem", 0.9);
        p.comment = "Display when an item gets damaged more than this fraction of its durability";
        DurabilityDisplayThresholdForItem = p.getDouble(0.9);
        p = config.get(Configuration.CATEGORY_GENERAL, "UpdateFrequency", 50);
        p.comment = "Update the HUD every XX game ticks (~100 = 1 second)";
        UpdateFrequency = p.getInt();
        p = config.get(Configuration.CATEGORY_GENERAL, "DurabilityLocationHorizontal", 10);
        p.comment = "The horizontal position of the durability icons. 0 is left, 400 is far right.";
        DurabilityLocationHorizontal = p.getInt();
        p = config.get(Configuration.CATEGORY_GENERAL, "DurabilityLocationVertical", 20);
        p.comment = "The vertical position of the durability icons. 0 is top, 200 is very bottom.";
        DurabilityLocationVertical = p.getInt();
        config.save();
    }
}