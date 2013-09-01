package zyin;

import java.io.File;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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

@Mod(modid = "ZyinMod", name = "Zyin's HUD", version = "0.4.1")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ZyinMod
{
	public static String CATEGORY_DISPLAY = "display";
	public static String CATEGORY_COORDINATES = "coordinates";
	public static String CATEGORY_DURABILITY = "durability";
	public static String CATEGORY_SAFEOVERLAY = "safe overlay";
	
	
    //Configurable values - display
    public static Boolean ShowInfoLine;
    public static Boolean ShowArmorDurability;
    public static Boolean ShowItemDurability;
    
    
    //Configurable values - coordinates
    public static Boolean ShowCoordinates;
    public static Boolean ShowCompass;
    
    
    //Configurable values - durability
    public static int DurabilityUpdateFrequency;
    public static double DurabilityDisplayThresholdForArmor;
    public static double DurabilityDisplayThresholdForItem;
    public static int DurabilityLocationHorizontal;
    public static int DurabilityLocationVertical;

    
    //Configurable values - safe overlay
    public static int SafeOverlayUpdateFrequency;
    public static int SafeOverlayDrawDistance;
    public static double SafeOverlayTransparency;
    public static Boolean SafeOverlayDisplayInNether;
    
    
    

    public static int DistanceMeasurerMode = 0;	//0=off, 1=simple, 2=complex
    public static int SafeOverlayMode = 0;	//0=off, 1=on
    private static int renderTicks = 0;	//how  many ticks have been rendered so far. used as a counter/timer.
    
    public static Configuration config = null;
    
    
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

        //needed for @ForgeSubscribe method subscriptions
        MinecraftForge.EVENT_BUS.register(this);
        
        LoadTickHandlers();
        LoadKeyHandlers();
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
    	
    }
    

    private void LoadTickHandlers()
    {
        //Tick Handlers (for drawing on the HUD)
        TickRegistry.registerTickHandler(new HUDTickHandler(), Side.CLIENT);
    }
    
    private void LoadKeyHandlers()
    {
        //Key Bind Handlers (for hotkeys)
        boolean[] repeat = {false};
        
        KeyBinding[] key_K = {new KeyBinding("Distance Measurer Toggle", 	Keyboard.KEY_K)};
        KeyBindingRegistry.registerKeyBinding(new DistanceMeasurerKeyHandler(key_K, repeat));
        
        KeyBinding[] key_L = {new KeyBinding("Safe Overlay Toggle", 		Keyboard.KEY_L)};
        KeyBindingRegistry.registerKeyBinding(new SafeOverlayKeyHandler(key_L, repeat));
    }

    private void LoadConfigSettings(File configFile)
    {
        //load configuration settings
        Configuration config = new Configuration(configFile);
        this.config = config;
        config.load();
        
        Property p;
        
        //CATEGORY_DISPLAY
        ShowInfoLine = config.get(CATEGORY_DISPLAY, "ShowInfoLine", true).getBoolean(true);
        ShowArmorDurability = config.get(CATEGORY_DISPLAY, "ShowArmorDurability", true).getBoolean(true);
        ShowItemDurability = config.get(CATEGORY_DISPLAY, "ShowItemDurability", true).getBoolean(true);
        

        //CATEGORY_COORDINATES
        ShowCoordinates = config.get(CATEGORY_COORDINATES, "ShowCoordinates", true).getBoolean(true);
        ShowCompass = config.get(CATEGORY_COORDINATES, "ShowCompass", true).getBoolean(true);
        
        
        //CATEGORY_DURABILITY
        p = config.get(CATEGORY_DURABILITY, "DurabilityDisplayThresholdForArmor", 0.9);
        p.comment = "Display when armor gets damaged more than this fraction of its durability";
        DurabilityDisplayThresholdForArmor = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITY, "DurabilityDisplayThresholdForItem", 0.9);
        p.comment = "Display when an item gets damaged more than this fraction of its durability";
        DurabilityDisplayThresholdForItem = p.getDouble(0.9);
        
        p = config.get(CATEGORY_DURABILITY, "DurabilityUpdateFrequency", 50);
        p.comment = "Update the HUD every XX game ticks (~100 = 1 second)";
        DurabilityUpdateFrequency = p.getInt();
        
        p = config.get(CATEGORY_DURABILITY, "DurabilityLocationHorizontal", 10);
        p.comment = "The horizontal position of the durability icons. 0 is left, 400 is far right.";
        DurabilityLocationHorizontal = p.getInt();
        
        p = config.get(CATEGORY_DURABILITY, "DurabilityLocationVertical", 20);
        p.comment = "The vertical position of the durability icons. 0 is top, 200 is very bottom.";
        DurabilityLocationVertical = p.getInt();
        
        
        //CATEGORY_SAFEOVERLAY
        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayUpdateFrequency", 250);
        p.comment = "The time in milliseconds between re-calculations of safe areas.";
        SafeOverlayUpdateFrequency = p.getInt(250);

        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDrawDistance", 30);
        p.comment = "How far away unsafe spots should be rendered around the player measured in blocks.";
        SafeOverlayDrawDistance = p.getInt(30);

        p = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayTransparency", 0.3);
        p.comment = "The transparency of the unsafe marks. Must be between greater than 0.1 and less than or equal to 1.";
        SafeOverlayTransparency = p.getDouble(0.3);
        
        SafeOverlayDisplayInNether = config.get(CATEGORY_SAFEOVERLAY, "SafeOverlayDisplayInNether", false).getBoolean(false);
        
        config.save();
    }
    
    @ForgeSubscribe
	public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
    	//render unsafe positions (hotkey check and cache calculations are done in this render method)
		SafeOverlay.instance.RenderUnsafePositions(event.partialTicks);
	}
    
    
}