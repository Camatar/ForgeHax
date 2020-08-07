package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "RiderDesync",
    description = "For entity force dismounting",
    category = Category.PLAYER
)
public class RiderDesync extends ToggleMod {

  private final BooleanSetting auto_update = newBooleanSetting()
      .name("auto-update")
      .description("Automatically update entity on dismount")
      .defaultTo(true)
      .build();

  private Entity dismountedEntity = null;
  private boolean forceUpdate = false;

  {
    newSimpleCommand()
        .name("remount")
        .description("Remount entity")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isEnabled()) {
            printWarning("Mod not enabled");
            return;
          }

          if (getLocalPlayer() == null || getWorld() == null) {
            printWarning("Must be ingame to use this command.");
            return;
          }

          if (dismountedEntity == null) {
            printWarning("No entity mounted");
            return;
          }

          getWorld().addEntity(dismountedEntity);
          getLocalPlayer().startRiding(dismountedEntity);

          printInform("Remounted entity " + dismountedEntity.getName());
        })
        .build();

    newSimpleCommand()
        .name("dismount")
        .description("Dismount entity")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isEnabled()) {
            printWarning("Mod not enabled");
            return;
          }

          if (getLocalPlayer() == null || getWorld() == null) {
            printWarning("Must be ingame to use this command.");
            return;
          }

          Entity mounted = getLocalPlayer().getRidingEntity();

          if (mounted == null) {
            printWarning("No entity mounted");
            return;
          }

          dismountedEntity = mounted;
          getLocalPlayer().stopRiding();
          mounted.remove();

          if (auto_update.getValue()) {
            forceUpdate = true;
            printInform("Dismounted entity " + mounted.getName() + " and forcing entity updates");
          } else {
            printInform("Dismounted entity " + mounted.getName());
          }
        })
        .build();

    newSimpleCommand()
        .name("force-update")
        .description("Force dismount entity")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          if (!isEnabled()) {
            printWarning("Mod not enabled");
            return;
          }

          if (getLocalPlayer() == null || getWorld() == null) {
            printWarning("Must be ingame to use this command.");
            return;
          }

          if (dismountedEntity == null) {
            printWarning("No entity to force remount");
            return;
          }

          forceUpdate = !forceUpdate;

          printInform("Force mounted entity = %s", forceUpdate ? "true" : "false");
        })
        .build();

    newSimpleCommand()
        .name("reset")
        .description("Reset the currently stored riding entity")
        .flag(EnumFlag.EXECUTOR_MAIN_THREAD)
        .executor(args -> {
          this.dismountedEntity = null;
          this.forceUpdate = false;
          printInform("Saved riding entity reset");
        })
        .build();
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + String.format(" [e = %s fu = %s]",
        dismountedEntity == null ? "null" : dismountedEntity.getName(),
        forceUpdate ? "true" : "false");
  }

  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (dismountedEntity == null || getMountedEntity() != null) {
      this.dismountedEntity = null;
      this.forceUpdate = false;
      return;
    }

    if (forceUpdate && dismountedEntity != null) {
      dismountedEntity.setPosition(getLocalPlayer().getPosX(), getLocalPlayer().getPosY(), getLocalPlayer().getPosZ());
      sendNetworkPacket(new CMoveVehiclePacket(dismountedEntity));
    }
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    this.dismountedEntity = null;
    this.forceUpdate = false;
  }
}
