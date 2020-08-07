package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Objects.isNull;

@RegisterMod(
    name = "CoordsFinder",
    description = "Logs coordinates of lightning strikes and teleports",
    category = Category.MISC
)
public class CoordsFinder extends ToggleMod {

  @SuppressWarnings("WeakerAccess")
  public final BooleanSetting logLightning = newBooleanSetting()
      .name("lighting")
      .description("log lightning strikes")
      .defaultTo(true)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final IntegerSetting minLightningDist = newIntegerSetting()
      .name("lighting-min-dist")
      .description("how far a lightning strike has to be from you to get logged")
      .min(0)
      .defaultTo(32)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final BooleanSetting logWolf = newBooleanSetting()
      .name("wolf")
      .description("log wolf teleports")
      .defaultTo(true)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final IntegerSetting minWolfDist = newIntegerSetting()
      .name("wolf-min-dist")
      .description("how far a wolf teleport has to be from you to get logged")
      .min(0)
      .defaultTo(256)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final BooleanSetting logPlayer = newBooleanSetting()
      .name("player")
      .description("log player teleports")
      .defaultTo(true)
      .build();

  @SuppressWarnings("WeakerAccess")
  public final IntegerSetting minPlayerDist = newIntegerSetting()
      .name("player-min-dist")
      .description("how far a player teleport has to be from you to get logged")
      .min(0)
      .defaultTo(256)
      .build();

  private final Path logPath = Common.getFileManager().getBaseResolve("logs/coordsfinder.log");
  private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

  private void logCoords(String name, double x, double y, double z) {
    int ix = MathHelper.floor(x);
    int iy = MathHelper.floor(y);
    int iz = MathHelper.floor(z);

    Common.printInform("%s > [x:%d, y:%d, z:%d]", name, ix, iy, iz);

    try {
      String toWrite = String
          .format("[%s][%s][%d,%d,%d]\n", timeFormat.format(new Date()), name, ix, iy, iz);
      Files
          .write(logPath, toWrite.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logCoordsOnMinecraftThread(String name, double x, double y, double z) {
    Common.addScheduledTask(() -> logCoords(name, x, y, z));
  }

  private boolean pastDistance(PlayerEntity player, BlockPos pos, double dist) {
    return player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) >= Math.pow(dist, 2);
  }

  @SubscribeEvent
  public void onPacketRecieving(PacketInboundEvent event) {
    PlayerEntity player = Common.getLocalPlayer();
    ClientWorld world = Common.getWorld();

    if (isNull(player) || isNull(world)) {
      return;
    }

    if (logLightning.getValue() && event.getPacket() instanceof SPlaySoundEffectPacket) {
      SPlaySoundEffectPacket packet = (SPlaySoundEffectPacket) event.getPacket();

      // in the SPacketSpawnGlobalEntity constructor, this is only set to 1 if it's a lightning bolt
      if (packet.getSound() != SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER) {
        return;
      }

      BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());

      if (pastDistance(player, pos, minLightningDist.getValue())) {
        logCoordsOnMinecraftThread("Lightning strike", pos.getX(), pos.getY(), pos.getZ());
      }
    } else if (event.getPacket() instanceof SEntityTeleportPacket) {
      SEntityTeleportPacket packet = (SEntityTeleportPacket) event.getPacket();
      Entity teleporting = world.getEntityByID(packet.getEntityId());
      BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());

      if (logWolf.getValue() && teleporting instanceof WolfEntity) {
        if (pastDistance(player, pos, minWolfDist.getValue())) {
          logCoordsOnMinecraftThread("Wolf teleport", packet.getX(), packet.getY(), packet.getZ());
        }
      } else if (logPlayer.getValue() && teleporting instanceof PlayerEntity) {
        if (pastDistance(player, pos, minPlayerDist.getValue())) {
          logCoordsOnMinecraftThread(String.format("Player teleport (%s)", teleporting.getName()),
              packet.getX(), packet.getY(), packet.getZ());
        }
      }
    }
  }
}
