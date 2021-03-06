package zyin.zyinhud.gui;

import zyin.zyinhud.util.Localization;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.gui.GuiSnooper;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.resources.GuiScreenTemporaryResourcePackSelect;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.EnumOptions;
import net.minecraft.client.settings.GameSettings;

/**
 * This GUI extends the default GuiOptions screen (when you click on "Options..." in the pause menu)
 * by adding additional buttons.
 * <p>
 * This is able to replace the default one by utilizing a hook in the GUITickHandler class.
 */
public class GuiOptionsOverride extends GuiOptions
{

    public GuiOptionsOverride(GuiScreen par1GuiScreen, GameSettings par2GameSettings)
    {
		super(par1GuiScreen, par2GameSettings);
	}

	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	super.initGui();
    	this.buttonList.add(new GuiButton(123456, this.width / 2 + 5, this.height / 6 + 59, 150, 20, Localization.get("gui.override.options.buttons.options")));

    }
    
    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
    	super.actionPerformed(par1GuiButton);
    	
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id == 123456)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiZyinHUDOptions(this));
            }
        }
    }
}






