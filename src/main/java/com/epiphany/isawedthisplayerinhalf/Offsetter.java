package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.networking.SetOffsetPacket;
import com.epiphany.isawedthisplayerinhalf.rendering.PlayerRendererWrapper;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Contains various functions to offset the actions taken by the player.
 */
public class Offsetter {
    public static final HashMap<UUID, Vec3d> playerOffsetMap = new HashMap<>();

    /**
     * Sets the initial offset for the client.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent loggedInEvent) {
        ClientPlayerEntity player = loggedInEvent.getPlayer();

        if (player != null) {
            Vec3d offsets = Config.getOffsets();

            setOffsets(player, offsets);

            if (player.world.isRemote)
                Networker.modChannel.sendToServer(new SetOffsetPacket(player, offsets));
        }
    }

    /**
     * Clears the offset maps when the client leaves a server.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent loggedOutEvent) {
        playerOffsetMap.clear();
        RenderingOffsetter.wrappedRendererMap.clear();
    }

    /**
     * Removes players from the offset map when they leave.
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public static void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        playerOffsetMap.remove(playerLoggedOutEvent.getPlayer().getUniqueID());
    }



    /**
     * Gets the offsets a player has.
     *
     * @param player The player to get the offsets from.
     *
     * @return The offsets a player has.
     */
    public static Vec3d getOffsets(PlayerEntity player) {
        UUID playerUUID = player.getUniqueID();
        return playerOffsetMap.containsKey(playerUUID) ? playerOffsetMap.get(playerUUID) : new Vec3d(0, 0, 0);
    }

    /**
     * Gets the offsets an entity has, if they are a player.
     *
     * @param entity The entity to get the offsets from.
     *
     * @return The offsets a entity has.
     */
    public static Vec3d getOffsets(Entity entity) {
        return entity instanceof PlayerEntity ? getOffsets((PlayerEntity) entity) : new Vec3d(0, 0, 0);
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param playerUUID The UUID of the player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(UUID playerUUID, Vec3d offsets) {
        playerOffsetMap.put(playerUUID, offsets);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            PlayerRendererWrapper wrappedRenderer = RenderingOffsetter.wrappedRendererMap.get(playerUUID);

            if (wrappedRenderer != null) {
                wrappedRenderer.setOffsets(offsets);

                if (offsets.equals(Vec3d.ZERO))
                    wrappedRenderer.reset();
            }
        });
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param player The player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(PlayerEntity player, Vec3d offsets) {
        setOffsets(player.getUniqueID(), offsets);
    }



    /**
     * Offsets the initial position of A raycast if the entity is a player.
     *
     * @param entity The entity whose raycast is being offset.
     * @param initialPosition The initial position of the raycast.
     *
     * @return The offset position for the raycast to use.
     */
    public static Vec3d offsetRaycast(Vec3d initialPosition, Entity entity) {
        if (entity instanceof PlayerEntity) {
            Vec3d offsets = getOffsets((PlayerEntity) entity);

            if (!offsets.equals(Vec3d.ZERO))
                return initialPosition.add(offsets);
        }

        return initialPosition;
    }

    /**
     * Offsets a projectile based on the offset of its shooter.
     *
     * @param projectile The projectile to offset the position of.
     * @param shooter The shooter of the projectile.
     */
    public static void offsetProjectile(Entity projectile, LivingEntity shooter) {
        if (shooter instanceof PlayerEntity) {
            Vec3d offsets = getOffsets((PlayerEntity) shooter);

            if (!offsets.equals(Vec3d.ZERO))
                projectile.setPosition(
                        projectile.getPosX() + offsets.x,
                        projectile.getPosY() + offsets.y,
                        projectile.getPosZ() + offsets.z
                );
        }
    }

    /**
     * Offsets the position of thrown items.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void offsetDroppedItem(ItemTossEvent itemTossEvent) {
        if (!itemTossEvent.isCanceled()) {
            Vec3d offsets = getOffsets(itemTossEvent.getPlayer());

            if (!offsets.equals(Vec3d.ZERO)) {
                ItemEntity itemEntity = itemTossEvent.getEntityItem();

                itemTossEvent.getEntityItem().setPosition(
                        itemEntity.getPosX() + offsets.x,
                        itemEntity.getPosY() + offsets.y,
                        itemEntity.getPosZ() + offsets.z
                );
            }
        }
    }

    /**
     * Gets the corrected distance squared from an entity to the player.
     *
     * @param entity The entity to use for the first position.
     * @param player The player to use for the second position.
     *
     * @return The distance, squared, between the entity and the player.
     */
    public static double modifiedGetDistanceSq(Entity entity, PlayerEntity player) {
        Vec3d offsets = getOffsets(player);
        return !offsets.equals(Vec3d.ZERO) ? entity.getDistanceSq(player.getPositionVec().add(offsets)) : entity.getDistanceSq(player);
    }

    /**
     * Gets the corrected distance from an entity to the player.
     *
     * @param entity1 The entity to use for the first position.
     * @param entity2 The entity to use for the second position.
     *
     * @return The distance between the first entity and second entity.
     */
    public static float modifiedGetDistance(Entity entity1, Entity entity2) {
        return entity2 instanceof PlayerEntity ? (float) Math.sqrt(modifiedGetDistanceSq(entity1, (PlayerEntity) entity2)) : entity1.getDistance(entity2);
    }

    /**
     * Creates a new BlockPos from an entity's position with offset.
     *
     * @param entity The entity to get the position from.
     *
     * @return The position of the entity.
     */
    public static BlockPos modifiedBlockPos(Entity entity) {
        Vec3d offsets = getOffsets(entity);
        return !offsets.equals(Vec3d.ZERO) ? new BlockPos(entity.getPositionVec().add(offsets)) : new BlockPos(entity);
    }


    /**
     * In-game config options implemented via chat "commands."
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerChat(ClientChatEvent clientChatEvent) {
        String[] possibleCommand = clientChatEvent.getOriginalMessage().toLowerCase().split(" ");

        if (possibleCommand[0].equals("::offsets")) {
            clientChatEvent.setCanceled(true);

        } else
            return;

        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (possibleCommand.length >= 2) {
            switch (possibleCommand[1]) {
                // What?
                case "what":Config.a();break;

                // Displays offsets.
                case "get":
                    player.sendMessage(new StringTextComponent("Current offsets: " + Config.offsetX.get() + ", " + Config.offsetY.get() + ", " + Config.offsetZ.get()));
                    break;

                // Sets the offsets for the player, and sends it to the server.
                case "set":
                    if (possibleCommand.length >= 5) {
                        try {
                            double x = Double.parseDouble(possibleCommand[2]), y = Double.parseDouble(possibleCommand[3]), z = Double.parseDouble(possibleCommand[4]);

                            Config.setOffsets(x, y, z);

                            setOffsets(player.getUniqueID(), new Vec3d(x, y, z));

                            if (player.world.isRemote)
                                Networker.modChannel.sendToServer(new SetOffsetPacket(player, x, y, z));

                            player.sendMessage(new StringTextComponent("Offsets set"));

                        } catch (NumberFormatException exception) {
                            player.sendMessage(new StringTextComponent("Usage: ::offsets set <x> <y> <z>"));
                        }

                    } else
                        player.sendMessage(new StringTextComponent("Usage: ::offsets set <x> <y> <z>"));

                    break;

                default:
                    player.sendMessage(new StringTextComponent("Usage: ::offsets (help|get)"));
                    player.sendMessage(new StringTextComponent("Usage: ::offsets set <x> <y> <z>"));
            }

        } else {
            player.sendMessage(new StringTextComponent("Usage: ::offsets (help|get)"));
            player.sendMessage(new StringTextComponent("Usage: ::offsets set <x> <y> <z>"));
        }
    }
}
