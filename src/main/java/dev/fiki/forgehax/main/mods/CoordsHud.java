package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.math.AlignHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.HudMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.DimensionType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.fiki.forgehax.main.Common.getWorld;

@RegisterMod(
    name = "CoordsHUD",
    description = "Display world coords",
    category = Category.RENDER
)
public class CoordsHud extends HudMod {
  private final BooleanSetting translate = newBooleanSetting()
      .name("translate")
      .description("show corresponding Nether or Overworld coords")
      .defaultTo(true)
      .build();

  private final BooleanSetting multiline = newBooleanSetting()
      .name("multiline")
      .description("show translated coords above")
      .defaultTo(true)
      .build();

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.BOTTOMRIGHT;
  }

  @Override
  protected int getDefaultOffsetX() {
    return 1;
  }

  @Override
  protected int getDefaultOffsetY() {
    return 1;
  }

  @Override
  protected double getDefaultScale() {
    return 1d;
  }

  double thisX;
  double thisY;
  double thisZ;
  double otherX;
  double otherZ;

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent ev) {
    ClientPlayerEntity player = Common.getLocalPlayer();
    thisX = player.getPosX();
    thisY = player.getPosY();
    thisZ = player.getPosZ();

    double thisFactor = DimensionType.field_236000_d_.equals(getWorld().func_234922_V_()) ? 8d : 1d;
    double otherFactor = thisFactor != 1d ? 1d : 8d;
    double travelFactor = thisFactor / otherFactor;
    otherX = thisX * travelFactor;
    otherZ = thisZ * travelFactor;
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();

    if (!translate.getValue() || (translate.getValue() && multiline.getValue())) {
      text.add(String.format("%01.1f, %01.0f, %01.1f", thisX, thisY, thisZ));
    }
    if (translate.getValue()) {
      if (multiline.getValue()) {
        text.add(String.format("(%01.1f, %01.1f)", otherX, otherZ));
      } else {
        text.add(String.format(
            "%01.1f, %01.0f, %01.1f (%01.1f, %01.1f)", thisX, thisY, thisZ, otherX, otherZ));
      }
    }

    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.getValue(), true, alignment.getValue().ordinal());
  }
}
