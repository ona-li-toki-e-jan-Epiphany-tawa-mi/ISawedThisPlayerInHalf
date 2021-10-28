package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.helpers.BytecodeHelper;
import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.epiphany.isawedthisplayerinhalf.networking.SetOffsetPacket;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
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
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> RenderingOffsetter.setOffsets(playerUUID, offsets));
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
        RenderingOffsetter.renderingOffsetsMap.clear();
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
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void offsetDroppedItem(ItemTossEvent itemTossEvent) {
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
     * Gets the corrected distance from an entity to the player.
     *
     * @param entity1 The entity to use for the first position.
     * @param entity2 The entity to use for the second position.
     *
     * @return The distance between the first entity and second entity.
     */
    public static float modifiedGetDistance(Entity entity1, Entity entity2) {
        return entity2 instanceof PlayerEntity ? (float) Math.sqrt(BytecodeHelper.modifiedGetDistanceSq(entity1, (PlayerEntity) entity2)) :
                entity1.getDistance(entity2);
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
                // What?
                case "what":Config.a();break;

                // Displays offsets.
                case "get":
                    player.sendMessage(new StringTextComponent(I18n.format("commands.swdthsplyrnhlf.offsets.get",
                            Config.offsetX.get(), Config.offsetY.get(), Config.offsetZ.get())));
                    break;

                // Resets the offsets for the player and notifies the server.
                case "reset":
                    if (!getOffsets(player).equals(Vec3d.ZERO)) {
                        Config.setOffsets(0, 0, 0);
                        setOffsets(player.getUniqueID(), Vec3d.ZERO);

                        if (player.world.isRemote)
                            Networker.modChannel.sendToServer(new SetOffsetPacket(player, 0, 0, 0));

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
                            setOffsets(player.getUniqueID(), new Vec3d(x, y, z));
                            if (player.world.isRemote)
                                Networker.modChannel.sendToServer(new SetOffsetPacket(player, x, y,z));

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
