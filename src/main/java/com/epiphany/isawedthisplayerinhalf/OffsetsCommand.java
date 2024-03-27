package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.config.ClientConfig;
import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
 * MIT License
 *
 * Copyright (c) 2021-2022 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

@OnlyIn(Dist.CLIENT)
public class OffsetsCommand {
    /**
     * In-game config options implemented via chat "commands."
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlayerChat(ClientChatEvent clientChatEvent) {
        String originalMessage = clientChatEvent.getOriginalMessage();
        String[] possibleCommand = originalMessage.split(" +");

        String possibleCommandStart = possibleCommand[0].toLowerCase();
        if (!"::offsets".equals(possibleCommandStart) && !"::ofs".equals(possibleCommandStart))
            return;

        clientChatEvent.setCanceled(true);
        // Ensures that the command is put into the player's history for when they press the up arrow to recall it.
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.ingameGUI.getChatGUI().addToSentMessages(originalMessage);

        ClientPlayerEntity player = minecraft.player;

        if (possibleCommand.length >= 2) {
            switch (possibleCommand[1].toLowerCase()) {
                case "get":
                    getOffsets(player, possibleCommand, minecraft);
                    break;

                case "set":
                    setOffsets(player, possibleCommand, originalMessage, minecraft);
                    break;

                case "reset":
                    resetOffsets(player);
                    break;

                case "what":ClientConfig.a();break;

                case "help":
                    sendHelpInformation(player);
                    break;

                default:
                    player.sendMessage(new TranslationTextComponent(
                            "commands.swdthsplyrnhlf.errors.unknown_command")
                            .applyTextStyle(TextFormatting.RED));
                    player.sendMessage(new StringTextComponent(
                            possibleCommand[0] + " " + possibleCommand[1] + I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer"))
                            .applyTextStyle(TextFormatting.RED));
                    sendUsageMessages(player);
            }

        } else {
            player.sendMessage(new StringTextComponent(ChatFormatting.RED +
                    I18n.format("commands.swdthsplyrnhlf.errors.incomplete_command")));
            player.sendMessage(new StringTextComponent(ChatFormatting.RED + originalMessage +
                    I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
            sendUsageMessages(player);
        }
    }



    /**
     * Displays the client's offsets.
     *
     * @param player The client.
     * @param possibleCommand The partially parsed command.
     * @param minecraft The current instance of Minecraft.
     */
    private static void getOffsets(ClientPlayerEntity player, String[] possibleCommand, Minecraft minecraft) {
        // Getting offsets of client.
        if (possibleCommand.length == 2) {
            Vec3d offsets = ClientConfig.getOffsets();

            player.sendMessage(new TranslationTextComponent(
                    "commands.swdthsplyrnhlf.offsets.get", offsets.x, offsets.y, offsets.z));

        // Getting offsets of a specified player.
        } else {
            String requestedPlayerName = possibleCommand[2];

            if (requestedPlayerName.length() > 16) {
                player.sendMessage(new TranslationTextComponent(
                        "commands.swdthsplyrnhlf.errors.unknown_player")
                        .applyTextStyle(TextFormatting.RED));
                return;
            }


            boolean success = false;

            for (PlayerEntity playerEntity : player.world.getPlayers())
                if (requestedPlayerName.equals(playerEntity.getName().getString())) {
                    Vec3d playerOffsets = Offsetter.getOffsetsOrNull(playerEntity);

                    if (playerOffsets != null) {
                        player.sendMessage(new TranslationTextComponent(
                                "commands.swdthsplyrnhlf.offsets.get.of_player", requestedPlayerName, playerOffsets.x, playerOffsets.y, playerOffsets.z));

                    } else if (!minecraft.isSingleplayer()) {
                        Networker.requestDisplayOffsets(requestedPlayerName);

                    } else
                        player.sendMessage(new TranslationTextComponent(
                                "commands.swdthsplyrnhlf.offsets.get.no_offsets", requestedPlayerName));

                    success = true;
                }

            if (!success)
                if (!minecraft.isSingleplayer()) {
                    Networker.requestDisplayOffsets(requestedPlayerName);

                } else
                    player.sendMessage(new TranslationTextComponent(
                            "commands.swdthsplyrnhlf.errors.unknown_player")
                            .applyTextStyle(TextFormatting.RED));
        }
    }

    /**
     * Attempts to set the client's offsets to new values.
     *
     * @param player The client.
     * @param possibleCommand The partially parsed command.
     * @param originalMessage The original command they typed.
     * @param minecraft The current instance of Minecraft.
     */
    private static void setOffsets(ClientPlayerEntity player, String[] possibleCommand, String originalMessage, Minecraft minecraft) {
        if (possibleCommand.length >= 5) {
            int failedParseDouble = 0;

            try {
                double x = Double.parseDouble(possibleCommand[2]);
                if (!Double.isFinite(x)) throw new NumberFormatException();
                failedParseDouble++;
                double y = Double.parseDouble(possibleCommand[3]);
                if (!Double.isFinite(y)) throw new NumberFormatException();
                failedParseDouble++;
                double z = Double.parseDouble(possibleCommand[4]);
                if (!Double.isFinite(z)) throw new NumberFormatException();

                Vec3d currentOffsets = Offsetter.getOffsets(player);
                if (currentOffsets.x == x && currentOffsets.y == y && currentOffsets.z == z) {
                    player.sendMessage(new TranslationTextComponent(
                            "commands.swdthsplyrnhlf.offsets.set.already_set", x, y, z)
                            .applyTextStyle(TextFormatting.RED));
                    return;
                }


                Offsetter.trySetClientOffsets(new Vec3d(x, y, z), (success) -> {
                    if (success) {
                        player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.set", x, y, z));

                    } else
                        player.sendMessage(new TranslationTextComponent(
                                "commands.swdthsplyrnhlf.offsets.set.server_error")
                                .applyTextStyle(TextFormatting.RED));
                });

            } catch (NumberFormatException exception) {
                // Grabs arguments up to the one that caused the error.
                StringBuilder necessaryArguments = new StringBuilder();
                for (int i = 0; i <= failedParseDouble; i++)
                    necessaryArguments.append(' ').append(possibleCommand[2 + i]);

                player.sendMessage(new TranslationTextComponent(
                        "commands.swdthsplyrnhlf.errors.number_expected")
                        .applyTextStyle(TextFormatting.RED));
                player.sendMessage(new StringTextComponent(
                        "::ofs set" + ChatFormatting.RED + necessaryArguments + I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer")));
            }

        } else {
            player.sendMessage(new TranslationTextComponent(
                    "commands.swdthsplyrnhlf.errors.incomplete_command")
                    .applyTextStyle(TextFormatting.RED));
            player.sendMessage(new StringTextComponent(
                    originalMessage + I18n.format("commands.swdthsplyrnhlf.errors.error_position_pointer"))
                    .applyTextStyle(TextFormatting.RED));
            player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.set.usage"));
        }

    }

    /**
     * Attempts to reset the client's offsets to (0, 0, 0).
     * @param player The client.
     */
    private static void resetOffsets(ClientPlayerEntity player) {
        if (!Offsetter.getOffsets(player).equals(Vec3d.ZERO)) {
            Offsetter.trySetClientOffsets(Vec3d.ZERO, (success) -> {
                if (success) {
                    player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.reset"));

                } else
                    player.sendMessage(new TranslationTextComponent(
                            "commands.swdthsplyrnhlf.offsets.reset.server_error")
                            .applyTextStyle(TextFormatting.RED));
            });

        } else
            player.sendMessage(new TranslationTextComponent(
                    "commands.swdthsplyrnhlf.offsets.reset.already_reset")
                    .applyTextStyle(TextFormatting.RED));
    }



    /**
     * Sends command help information to the client.
     *
     * @param player The client.
     */
    private static void sendHelpInformation(ClientPlayerEntity player) {
        player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.help"));
        sendUsageMessages(player);
    }

    /**
     * Sends command usage information to the client.
     *
     * @param player The client.
     */
    private static void sendUsageMessages(ClientPlayerEntity player) {
        player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.usage_title"));
        player.sendMessage(new StringTextComponent(
                "    " + I18n.format("commands.swdthsplyrnhlf.offsets.help.usage")));
        player.sendMessage(new StringTextComponent(
                "    " + I18n.format("commands.swdthsplyrnhlf.offsets.get.usage")));
        player.sendMessage(new StringTextComponent(
                "    " + I18n.format("commands.swdthsplyrnhlf.offsets.reset.usage")));
        player.sendMessage(new StringTextComponent(
                "    " + I18n.format("commands.swdthsplyrnhlf.offsets.set.usage")));
    }
}
