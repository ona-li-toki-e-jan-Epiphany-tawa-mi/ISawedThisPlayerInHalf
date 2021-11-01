package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Contains various functions to offset the actions taken by the player.
 */
public class Offsetter {
    private static final HashMap<UUID, Vec3d> playerOffsetMap = new HashMap<>();

    /**
     * Gets the offsets a player has.
     *
     * @param playerEntity The player to get the offsets from.
     *
     * @return The offsets a player has.
     */
    public static Vec3d getOffsets(PlayerEntity playerEntity) {
        Vec3d offsets = playerOffsetMap.get(playerEntity.getUniqueID());
        return offsets != null ? offsets : Vec3d.ZERO;
    }

    /**
     * Gets the offsets an entity has, if they are a player.
     *
     * @param entity The entity to get the offsets from.
     *
     * @return The offsets an entity has.
     */
    public static Vec3d getOffsets(Entity entity) {
        return entity instanceof PlayerEntity ? getOffsets((PlayerEntity) entity) : Vec3d.ZERO;
    }

    /**
     * Returns the offsets a player has, or null, if they do not have any.
     *
     * @param playerEntity The player to get the offsets of.
     *
     * @return The offsets of the player, or null.
     */
    public static Vec3d getOffsetsOrNull(PlayerEntity playerEntity) {
        return playerOffsetMap.get(playerEntity.getUniqueID());
    }

    /**
     * Returns an unmodifiable set containing the UUIDs of the players that have offsets.
     *
     * @return A set containing the UUIDs of offset players.
     */
    public static Set<UUID> getOffsetPlayerUUIDs() {
        return Collections.unmodifiableSet(playerOffsetMap.keySet());
    }

    /**
     * Sets the offsets for the given player.
     *
     * @param playerEntity The player to set the offsets of.
     * @param offsets The offsets to set to the player.
     */
    public static void setOffsets(PlayerEntity playerEntity, Vec3d offsets) {
        UUID playerUUID = playerEntity.getUniqueID();

        playerOffsetMap.put(playerUUID, offsets);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> RenderingOffsetter.setOffsets(playerEntity, offsets));
    }



    /**
     * Loads the clients offsets when the world loads. And, if on a dedicated server, sends them to it.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onPostHandleJoinGame() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        Vec3d offsets = Config.getOffsets();

        setOffsets(player, offsets);
        if (player.world.isRemote)
            Networker.sendServerOffsets(offsets);
    }

    /**
     * Clears the offset maps when the client leaves a server.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggedOutEvent loggedOutEvent) {
        playerOffsetMap.clear();
        RenderingOffsetter.clearAllOffsets();
    }

    /**
     * Request the offsets of players that are being loaded from the server.
     *
     * @param entityId The entity id the entity.
     * @param entity The entity being loaded.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onPostEntityLoad(int entityId, Entity entity) {
        if (entity instanceof PlayerEntity && entity.world.isRemote && !entity.equals(Minecraft.getInstance().player))
            Networker.requestOffsetsFor(entityId);
    }

    /**
     * Unloads players from the offset map when they are being unloaded from the world.
     *
     * @param entity The entity being unloaded.
     */
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    public static void onEntityUnload(Entity entity) {
        if (entity instanceof PlayerEntity && entity.world.isRemote && !entity.equals(Minecraft.getInstance().player)) {
            UUID playerUUID = entity.getUniqueID();

            playerOffsetMap.remove(playerUUID);
            RenderingOffsetter.unsetOffsets(playerUUID);
        }
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
     * Offsets the position of thrown items.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onItemDropped(ItemTossEvent itemTossEvent) {
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

    /**
     * In-game config options implemented via chat "commands."
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlayerChat(ClientChatEvent clientChatEvent) {
        String message = clientChatEvent.getOriginalMessage().toLowerCase();
        String[] possibleCommand = message.split(" ");

        if (!possibleCommand[0].equals("::offsets") && !possibleCommand[0].equals("::ofs"))
            return;

        clientChatEvent.setCanceled(true);
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (possibleCommand.length >= 2) {
            switch (possibleCommand[1]) {
                case "what":Config.a();break;

                // Displays offsets.
                case "get":
                    Vec3d offsets = Config.getOffsets();

                    player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.get",
                            offsets.x, offsets.y, offsets.z)));
                    break;

                // Resets the offsets for the player and notifies the server.
                case "reset":
                    if (!getOffsets(player).equals(Vec3d.ZERO)) {
                        Config.setOffsets(0, 0, 0);
                        setOffsets(player, Vec3d.ZERO);
                        if (player.world.isRemote)
                            Networker.sendServerOffsets(0, 0, 0);

                        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.reset")));

                    } else
                        player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                                I18n.format("commands.swdthsplyrnhlf.offsets.reset.already_reset")));

                    break;

                // Sets the offsets for the player and sends it to the server.
                case "set":
                    if (possibleCommand.length >= 5) {
                        int failedParseDouble = 0;

                        try {
                            double x = Double.parseDouble(possibleCommand[2]);
                            failedParseDouble++;
                            double y = Double.parseDouble(possibleCommand[3]);
                            failedParseDouble++;
                            double z = Double.parseDouble(possibleCommand[4]);

                            Vec3d currentOffsets = getOffsets(player);
                            if (currentOffsets.x == x && currentOffsets.y == y && currentOffsets.z == z) {
                                player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                                        I18n.format("commands.swdthsplyrnhlf.offsets.set.already_set", x, y, z)));
                                break;
                            }

                            Config.setOffsets(x, y, z);
                            setOffsets(player, new Vec3d(x, y, z));
                            if (player.world.isRemote)
                                Networker.sendServerOffsets(x,y,z);

                            player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.set", x, y, z)));

                        } catch (NumberFormatException exception) {
                            StringBuilder necessaryArguments = new StringBuilder();
                            for (int i = 0; i <= failedParseDouble; i++)
                                necessaryArguments.append(' ').append(possibleCommand[2 + i]);

                            player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                                    I18n.format("commands.swdthsplyrnhlf.errors.number_expected")));
                            player.sendMessage(new StringTextComponent("::ofs set" + ChatFormatting.RED + necessaryArguments +
                                    I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
                        }

                    } else {
                        player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                                I18n.format("commands.swdthsplyrnhlf.errors.incomplete_command")));
                        player.sendMessage(new StringTextComponent(ChatFormatting.RED + message +
                                I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
                        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.set.usage")));
                    }

                    break;

                // Displays help information.
                case "help":
                    player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.help")));
                    sendUsageMessages(player);

                    break;

                default:
                    player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                            I18n.format("commands.swdthsplyrnhlf.errors.unknown_command")));
                    player.sendMessage(new StringTextComponent(ChatFormatting.RED + possibleCommand[0] + " " + possibleCommand[1] +
                            I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
                    sendUsageMessages(player);
            }

        } else {
            player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                    I18n.format("commands.swdthsplyrnhlf.errors.incomplete_command")));
            player.sendMessage(new StringTextComponent(ChatFormatting.RED + message +
                    I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
            sendUsageMessages(player);
        }
    }

    /**
     * Sends usage information to player about [::offsets]'s subcommands.
     *
     * @param player The player to send the messages to.
     */
    @OnlyIn(Dist.CLIENT)
    private static void sendUsageMessages(ClientPlayerEntity player) {
        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.help.usage")));
        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.get.usage")));
        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.reset.usage")));
        player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.set.usage")));
    }
}
