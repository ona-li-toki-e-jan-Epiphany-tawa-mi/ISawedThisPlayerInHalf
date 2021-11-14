package com.epiphany.isawedthisplayerinhalf;

import com.epiphany.isawedthisplayerinhalf.networking.Networker;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class OffsetsCommand {
    // TODO (Maybe) add the ability to ::ofs get the offsets of other players.
    // TODO Improve parsing.
    /**
     * In-game config options implemented via chat "commands."
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlayerChat(ClientChatEvent clientChatEvent) {
        String originalMessage = clientChatEvent.getOriginalMessage().toLowerCase();
        String[] possibleCommand = originalMessage.split(" ");

        if (!possibleCommand[0].equals("::offsets") && !possibleCommand[0].equals("::ofs"))
            return;

        clientChatEvent.setCanceled(true);

        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;

        if (possibleCommand.length >= 2) {
            switch (possibleCommand[1]) {
                case "get":
                    getOffsets(player);
                    break;

                case "set":
                    setOffsets(player, possibleCommand, originalMessage, minecraft);
                    break;

                case "reset":
                    resetOffsets(player, minecraft);
                    break;

                case "what":Config.a();break;

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
     */
    private static void getOffsets(ClientPlayerEntity player) {
        Vec3d offsets = Config.getOffsets();

        player.sendMessage(new TranslationTextComponent(
                "commands.swdthsplyrnhlf.offsets.get", offsets.x, offsets.y, offsets.z));
    }

    /**
     * Attempts to set the client's offsets to new values.
     *
     * @param player The client.
     * @param possibleCommand The partially-parsed command.
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


                Config.setOffsets(x, y, z);
                Offsetter.setOffsets(player, new Vec3d(x, y, z));

                if (!minecraft.isSingleplayer())
                    Networker.sendServerOffsets(x,y,z);

                player.sendMessage(new TranslationTextComponent(
                        "commands.swdthsplyrnhlf.offsets.set", x, y, z));

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
     *
     * @param player The client.
     * @param minecraft The current instance of Minecraft.
     */
    private static void resetOffsets(ClientPlayerEntity player, Minecraft minecraft) {
        if (!Offsetter.getOffsets(player).equals(Vec3d.ZERO)) {
            Config.setOffsets(0, 0, 0);
            Offsetter.setOffsets(player, Vec3d.ZERO);

            if (!minecraft.isSingleplayer())
                Networker.sendServerOffsets(0, 0, 0);

            player.sendMessage(new TranslationTextComponent("commands.swdthsplyrnhlf.offsets.reset"));

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