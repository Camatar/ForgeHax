package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.gui.ClickGui;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.OptionsSoundsScreen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 9/5/2017.
 */
@RegisterMod
public class GuiMove extends ToggleMod {
  
  public GuiMove() {
    super(Category.MISC, "GuiMove", false, "move with a gui open");
  }
  
  @SubscribeEvent
  public void LocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    KeyBinding[] keys = {
        Common.getGameSettings().keyBindForward,
        Common.getGameSettings().keyBindBack,
        Common.getGameSettings().keyBindLeft,
        Common.getGameSettings().keyBindRight,
        Common.getGameSettings().keyBindJump,
        Common.getGameSettings().keyBindSprint
    };
    if (Common.getDisplayScreen() instanceof OptionsScreen
        || Common.getDisplayScreen() instanceof VideoSettingsScreen
        || Common.getDisplayScreen() instanceof OptionsSoundsScreen
        || Common.getDisplayScreen() instanceof ContainerScreen
        || Common.getDisplayScreen() instanceof IngameMenuScreen
        || Common.getDisplayScreen() instanceof ClickGui) {
      for (KeyBinding bind : keys) {
        KeyBinding.setKeyBindState(bind.getKey(),
            InputMappings.isKeyDown(Common.getMainWindow().getHandle(), bind.getKey().getKeyCode()));
      }
    } else if (Common.getDisplayScreen() == null) {
      for (KeyBinding bind : keys) {
        if(InputMappings.isKeyDown(Common.getMainWindow().getHandle(), bind.getKey().getKeyCode())) {
          KeyBinding.setKeyBindState(bind.getKey(), false);
        }
      }
    }
  }
}