/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.commands;

import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.core.utils.helpers.TextAction;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.overlays.ui.ChangelogUI;
import com.wynntils.modules.richpresence.RichPresenceModule;
import com.wynntils.modules.richpresence.profiles.RichProfile;
import com.wynntils.modules.utilities.managers.KeyManager;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

import java.util.Collections;
import java.util.List;

public class CommandWynntils extends CommandBase implements IClientCommand {

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "wynntils";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/wynntils <command>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            execute(server, sender, new String[]{"help"});
            return;
        }

        if (TextAction.isCommandPrefix(args[0])) {
            TextAction.processCommand(args);
            return;
        }

        switch (String.join("", args).toLowerCase()) {
            case "donate":
                StringTextComponent c = new StringTextComponent("You can donate to Wynntils at: ");
                c.getStyle().withColor(TextFormatting.AQUA);
                StringTextComponent url = new StringTextComponent("https://www.patreon.com/Wynntils");
                url.getStyle()
                        .withColor(TextFormatting.LIGHT_PURPLE)
                        .setUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/Wynntils"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click here to open in your browser.")));

                sender.sendMessage(c.append(url));
                break;
            case "help":
            case "help1":
                StringTextComponent text = new StringTextComponent("");
                text.getStyle().withColor(TextFormatting.GOLD);
                text.append("Wynntils' command list: ");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " help", "This shows a list of all available commands for Wynntils.");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " discord", "This provides you with an invite to our Discord server.");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " version", "This shows the installed Wynntils version.");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " changelog [major/latest]", "This shows the changelog of your installed version.");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " reloadapi", "This reloads all API data.");
                text.append("\n");
                addCommandDescription(text, "-wynntils", " donate", "This provides our Patreon link.");
                text.append("\n");
                addCommandDescription(text, "-", "token", "This provides a clickable token for you to create a Wynntils account to manage your cosmetics.");
                text.append("\n");
                addCommandDescription(text, "-", "compass", "This makes your compass point towards an x and z or a direction (e.g. north, SE).");
                text.append("\n");
                addCommandDescription(text, "-", "territory", "This makes your compass point towards a specified territory.");
                text.append("\n")
                        .append(new StringTextComponent("Page 1 (out of 2) "))
                        .append(new StringTextComponent(">>>").setStyle(new Style()
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils help 2"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to the next page")))));
                sender.sendMessage(text);
                break;
                /*Since we combine all arguments, to get the second page of help the case could be "help2" for "/wynntils help 2".*/
            case "help2":
                StringTextComponent text1 = new StringTextComponent("");
                text1.getStyle().withColor(TextFormatting.GOLD);
                text1.append("Wynntils' command list: ");
                text1.append("\n");
                addCommandDescription(text1, "-", "lootrun", "This allows you to record and display lootrun paths.");
                text1.append("\n");
                addCommandDescription(text1, "-", "s", "This lists all online worlds in Wynncraft and the details of each world.");
                text1.append("\n");
                addCommandDescription(text1, "-", "exportdiscoveries", "This exports all discovered discoveries as a .csv file.");
                text1.append("\n");
                addCommandDescription(text1, "-", "forceupdate", "This downloads and installs the latest successful build.");
                text1.append("\n")
                        .append(new StringTextComponent("<<<").setStyle(new Style()
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils help 1"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to the next page")))))
                        .append(new StringTextComponent(" Page 2 (out of 2)"));
                sender.sendMessage(text1);
                break;
            case "discord":
                StringTextComponent msg = new StringTextComponent("You're welcome to join our Discord server at:\n");
                msg.getStyle().withColor(TextFormatting.GOLD);
                String discordInvite = WebManager.getApiUrls() == null ? null : WebManager.getApiUrls().get("DiscordInvite");
                StringTextComponent link = new StringTextComponent(discordInvite == null ? "<Wynntils servers are down>" : discordInvite);
                link.getStyle().withColor(TextFormatting.DARK_AQUA);
                if (discordInvite != null) {
                    link.getStyle()
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click here to join our Discord server.")));

                    RichProfile.OverlayManager o = RichPresenceModule.getModule().getRichPresence().getOverlayManager();
                    if (o != null) {
                        o.openGuildInvite(discordInvite.replace("https://discord.gg/", ""));
                    }
                }
                sender.sendMessage(msg.append(link));
                break;
            case "version":
                handleModVersion(sender);
                break;
            case "reloadapi":
                WebManager.reset();
                WebManager.setupUserAccount();
                WebManager.setupWebApi(false);
                break;
            case "changelog":
                new Delay(() -> ChangelogUI.loadChangelogAndShow(CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE, false), 1);
                break;
            case "changeloglatest":
                new Delay(() -> ChangelogUI.loadChangelogAndShow(CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE, true), 1);
                break;
            case "changelogmajor":
                new Delay(() -> ChangelogUI.loadChangelogAndShow(true, false), 1);
                break;
            case "debug":
                if (!Reference.developmentEnvironment) {
                    ITextComponent message = new StringTextComponent(TextFormatting.RED + "You can't use this command outside a development environment");

                    Minecraft.getInstance().player.sendMessage(message);
                    return;
                }

                Textures.loadTextures();
                break;
            default:
                execute(server, sender, new String[] {"help"});
        }
    }

    private static void addCommandDescription(ITextComponent text, String prefix, String name, String description) {
        StringTextComponent prefixText = new StringTextComponent(prefix);
        prefixText.getStyle().withColor(TextFormatting.DARK_GRAY);
        text.append(prefixText);

        StringTextComponent nameText = new StringTextComponent(name);
        nameText.getStyle().withColor(TextFormatting.RED);
        text.append(nameText);

        text.append(" ");

        StringTextComponent descriptionText = new StringTextComponent(description);
        descriptionText.getStyle().withColor(TextFormatting.GRAY);
        text.append(descriptionText);
    }

    private static void handleModVersion(ICommandSender sender) {
        if (Reference.developmentEnvironment) {
            StringTextComponent text = new StringTextComponent("Wynntils is running in a development environment.");
            text.getStyle().withColor(TextFormatting.GOLD);
            sender.sendMessage(text);
            return;
        }

        StringTextComponent releaseStreamText;
        StringTextComponent buildText;
        if (CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE) {
            releaseStreamText = new StringTextComponent("You are using Stable release stream: ");
            buildText = new StringTextComponent("Version " + Reference.VERSION);
        } else {
            releaseStreamText = new StringTextComponent("You are using Cutting Edge release stream: ");
            if (Reference.BUILD_NUMBER == -1) {
                buildText = new StringTextComponent("Unknown Build");
            } else {
                buildText = new StringTextComponent("Build " + Reference.BUILD_NUMBER);
            }
        }
        releaseStreamText.getStyle().withColor(TextFormatting.GOLD);
        buildText.getStyle().withColor(TextFormatting.YELLOW);
        StringTextComponent versionText = new StringTextComponent("");
        versionText.append(releaseStreamText);
        versionText.append(buildText);

        StringTextComponent updateCheckText;
        TextFormatting color;
        if (WebManager.getUpdate().updateCheckFailed()) {
            updateCheckText = new StringTextComponent("Wynntils failed to check for updates. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to try again.");
            color = TextFormatting.DARK_RED;
        } else if (WebManager.getUpdate().hasUpdate()) {
            updateCheckText = new StringTextComponent("Wynntils is currently outdated. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to update now.");
            color = TextFormatting.DARK_RED;
        } else {
            updateCheckText = new StringTextComponent("Wynntils was up-to-date when last checked. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to check for updates.");
            color = TextFormatting.DARK_GREEN;
        }
        updateCheckText.getStyle().withColor(color);
        sender.sendMessage(updateCheckText);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "discord", "version", "changelog", "reloadapi", "donate");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "changelog":
                    return getListOfStringsMatchingLastWord(args, "major", "latest");
                case "help":
                    return getListOfStringsMatchingLastWord(args, "1", "2");
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
