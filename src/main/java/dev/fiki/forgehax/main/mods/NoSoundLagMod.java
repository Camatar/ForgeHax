package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

@RegisterMod(
    name = "NoSoundLag",
    description = "lag exploit fix",
    category = Category.MISC
)
public class NoSoundLagMod extends ToggleMod {
  private static final Set<SoundEvent> BLACKLIST = Sets.newHashSet(
      SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
      SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA,
      SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
      SoundEvents.ITEM_ARMOR_EQUIP_IRON,
      SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
      SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
      SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
      SoundEvents.ITEM_ARMOR_EQUIP_TURTLE
  );

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlaySoundEffectPacket) {
      SPlaySoundEffectPacket packet = (SPlaySoundEffectPacket) event.getPacket();
      if (BLACKLIST.contains(packet.getSound())) {
        event.setCanceled(true);
      }

    }
  }
}
