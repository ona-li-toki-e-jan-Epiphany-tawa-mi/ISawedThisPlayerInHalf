package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* TODO Check this function in PlayerEntity.
for(LivingEntity livingentity : this.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
    if (livingentity != this && livingentity != targetEntity && !this.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingentity).hasMarker()) && this.getDistanceSq(livingentity) < 9.0D) {
        livingentity.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F))));
        livingentity.attackEntityFrom(DamageSource.causePlayerDamage(this), f3);
    }
}
*/
// TODO TemptGoal.
// TODO Enderman.StareGoal.
// TODO Enderman.FindPlayerGoal.
// TODO Add @Nullable to methods where possible.
// TODO Bow sounds

// TODO (MAYBE MAYBE NOT) Expose an API for other mods to interact with this one.

/**
 * This mod splits apart the player, freeing the torso from the legs.
 *
 * @see <a href="https://github.com/ona-li-toki-e-jan-Epiphany-tawa-mi/ISawedThisPlayerInHalf">Github repo.</a>
 * @see <a href="https://www.curseforge.com/minecraft/mc-mods/i-sawed-this-player-in-half">Curseforge page.</a>
 */
@Mod("swdthsplyrnhlf")
public class ISawedThisPlayerInHalf {
    public static final String MOD_ID = "swdthsplyrnhlf";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ISawedThisPlayerInHalf() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ISawedThisPlayerInHalf.class);
        MinecraftForge.EVENT_BUS.register(Offsetter.class);

        Networker.registerPackets();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent fmlClientSetupEvent) {
        MinecraftForge.EVENT_BUS.register(OffsetsCommand.class);

        Config.enable();
        RenderingOffsetter.replacePlayerRenderers();
    }
}
