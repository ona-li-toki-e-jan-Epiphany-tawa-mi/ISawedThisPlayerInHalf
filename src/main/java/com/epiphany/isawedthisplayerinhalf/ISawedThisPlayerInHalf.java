package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// TODO Offset player nametags.
/* TODO Check this function in PlayerEntity.
for(LivingEntity livingentity : this.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
    if (livingentity != this && livingentity != targetEntity && !this.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingentity).hasMarker()) && this.getDistanceSq(livingentity) < 9.0D) {
        livingentity.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F))));
        livingentity.attackEntityFrom(DamageSource.causePlayerDamage(this), f3);
    }
}
*/
// TODO Look more into LookAtGoal.
// TODO Have passive mobs follow bait towards a player's offset position.
// TODO Firework crossbows need to be offset.

@Mod("swdthsplyrnhlf")
public class ISawedThisPlayerInHalf {
    public static final String MOD_ID = "swdthsplyrnhlf";

    public ISawedThisPlayerInHalf() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ISawedThisPlayerInHalf.class);
        MinecraftForge.EVENT_BUS.register(Offsetter.class);

        Networker.registerPackets();
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent fmlClientSetupEvent) {
        Config.onEnable();
        RenderingOffsetter.replacePlayerRenderers();
    }
}
