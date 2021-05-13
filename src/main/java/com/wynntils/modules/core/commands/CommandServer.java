/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.google.common.collect.Lists;
import com.wynntils.Reference;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

import java.util.*;


public class CommandServer extends CommandBase implements IClientCommand {
    private List<String> serverTypes = Lists.newArrayList("WC", "lobby", "GM", "DEV", "WAR", "HB");

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "s";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/s <command> [options]\n\ncommands:\nl,ls,list | list available servers\ni,info | get info about a server\n\nmore detailed help:\n/s <command> help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (Reference.onServer) {
            if (args.length >= 1) {
                // String option = args[0];
                switch (args[0].toLowerCase()) {
                    case "list":
                    case "ls":
                    case "l":
                        serverList(server, sender, Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "info":
                    case "i":
                        serverInfo(server, sender, Arrays.copyOfRange(args, 1, args.length));
                        break;
                    default:
                        throw new CommandException(getUsage(sender));
                }
            } else {
                throw new CommandException(getUsage(sender));
            }
        }
    }

    private void serverList(MinecraftServer server, ICommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        String selectedType = null;

        for (String arg : args) {
            for (String type : serverTypes) {
                if (arg.equalsIgnoreCase(type)) {
                    selectedType = type;
                    break;
                }
            }
            switch (arg.toLowerCase()) {
                case "group":
                case "g":
                    options.add("group");
                    break;
                case "sort":
                case "s":
                    options.add("sort");
                    options.add("group");
                    break;
                case "count":
                case "c":
                    options.add("count");
                    break;
                case "help":
                case "h":
                    options.add("help");
                    break;
            }
        }

        StringTextComponent text;
        if (options.contains("help")) {
            text = new StringTextComponent(
                    "Usage: /s list [type] [options]\n order of types and options does not matter\nDefault: print all servers oldest to new\n\ntypes:\n");
            for (String type : serverTypes) {
                text.append(String.format("  %s\n", type));
            }
            text.append("options:\n");
            text.append("  g, group : group servers by type\n");
            text.append("  s, sort : sort servers alphabetically, sets group flag\n");
            text.append("  c, count : print amount of online servers\n");
            text.append("  h, help : this help\n");
            sender.sendMessage(text);
            return;
        }

        int messageId = Utils.getRandom().nextInt(Integer.MAX_VALUE);
        ChatOverlay.getChat().printUnloggedChatMessage(new StringTextComponent(TextFormatting.GRAY + "Calculating Servers..."), messageId);

        String finalSelectedType = selectedType;
        Utils.runAsync(() -> {
            try {
                Map<String, List<String>> onlinePlayers = WebManager.getOnlinePlayers();

                if (options.contains("group") && finalSelectedType == null) {
                    StringTextComponent toEdit = new StringTextComponent("Available servers" +
                            (options.contains("count") ? String.format(" (%d)", onlinePlayers.size()): "") + ":\n");

                    for (String type : serverTypes.subList(0, serverTypes.size() - 1)) {
                        toEdit.append(getFilteredServerList(onlinePlayers, type, options));
                        toEdit.append("\n");
                    }
                    toEdit.append(getFilteredServerList(onlinePlayers, serverTypes.get(serverTypes.size() - 1), options));

                    ChatOverlay.getChat().printUnloggedChatMessage(toEdit, messageId);  // updates the message
                    return;
                }

                if (finalSelectedType == null) {
                    ChatOverlay.getChat().printUnloggedChatMessage(
                            getFilteredServerList(onlinePlayers, "", options), messageId
                    );  // updates the message
                    return;
                }

                ChatOverlay.getChat().printUnloggedChatMessage(
                        getFilteredServerList(onlinePlayers, finalSelectedType, options), messageId
                );  // updates the message
            } catch (Exception ex) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent(
                                TextFormatting.RED +
                                "An error occurred while trying to get the servers!"
                        ).setStyle(new Style().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new StringTextComponent(TextFormatting.RED + ex.getMessage())
                                ))),
                        messageId
                );

                ex.printStackTrace();
            }
        });
    }

    private static void serverInfo(MinecraftServer server, ICommandSender sender, String[] args) {
        int messageId = Utils.getRandom().nextInt(Integer.MAX_VALUE);
        ChatOverlay.getChat().printUnloggedChatMessage(
                new StringTextComponent(TextFormatting.GRAY + "Calculating Server Information..."
                ), messageId);

        Utils.runAsync(() -> {
            if (args.length == 0) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent("Usage: /s info <serverID>"), messageId);
                return;
            }
            if (args.length > 1) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent("Too many arguments\nUsage: /s info <serverID>"), messageId);
                return;
            }
            if (args[0].equalsIgnoreCase("help")) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent("Usage: /s info <serverID>"), messageId);
                return;
            }
            // args.length == 1 and no help
            try {
                Map<String, List<String>> onlinePlayers = WebManager.getOnlinePlayers();
                for (String serverName : onlinePlayers.keySet()) {
                    if (args[0].equalsIgnoreCase(serverName)) {
                        StringTextComponent text = new StringTextComponent(String.format("%s: ", serverName));
                        StringTextComponent playerText = new StringTextComponent("");

                        List<String> players = onlinePlayers.get(serverName);

                        if (players.size() > 0) {
                            for (String player : players.subList(0, players.size() - 1)) {
                                playerText.append(String.format("%s, ", player));
                            }
                            playerText.append(players.get(players.size() - 1));
                            playerText.getStyle().withColor(TextFormatting.GRAY);
                            text.append(playerText);
                        }

                        text.append("\nTotal online players: ");
                        StringTextComponent playerCountText = new StringTextComponent(Integer.toString(players.size()));
                        playerCountText.getStyle().withColor(TextFormatting.GRAY);
                        text.append(playerCountText);

                        ChatOverlay.getChat().printUnloggedChatMessage(text, messageId);
                        return;
                    }
                }
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent(String.format("Unknown server ID: %s", args[0])), messageId);

            } catch (Exception e) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new StringTextComponent(
                                TextFormatting.RED +
                                        "An error occurred while trying to get the servers!"
                        ).setStyle(new Style().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new StringTextComponent(TextFormatting.RED + e.getMessage())
                        ))),
                        messageId
                );

                e.printStackTrace();
            }
        });
    }

    private static StringTextComponent getFilteredServerList(Map<String, List<String>> onlinePlayers,
                                                       String filter,
                                                       List<String> options) {
        StringTextComponent text = new StringTextComponent("");
        StringTextComponent serverListText = new StringTextComponent("");

        int serverCount = 0;
        for (String serverName : options.contains("sort") ? new TreeSet<>(onlinePlayers.keySet()) : onlinePlayers.keySet()) {
            if (serverName.toLowerCase().contains(filter.toLowerCase())) {
                StringTextComponent serverText = new StringTextComponent(String.format("%s ", serverName));
                if (onlinePlayers.get(serverName).size() >= 48) {serverText.getStyle().withColor(TextFormatting.RED);}
                else {serverText.getStyle().withColor(TextFormatting.GREEN);}
                serverListText.append(serverText);
                serverCount++;
            }
        }

        if (filter.equals("")) {
            text.append("Available servers" +
                    (options.contains("count") ? String.format(" (%d)", onlinePlayers.size()): "") + ":\n");
        } else if (options.contains("count")) {
            text.append(String.format("%s (%d):\n", filter, serverCount));
        } else {
            text.append(String.format("%s:\n", filter));
        }

        if (serverCount == 0) {
            serverListText.append("none");
            serverListText.getStyle().withColor(TextFormatting.DARK_GRAY);
            text.getStyle().withColor(TextFormatting.GRAY);
        }

        text.append(serverListText);

        return text;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "info");
        }
        switch (args[0].toLowerCase()) {
            case "list":
            case "ls":
            case "l":
                List<String> arguments = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                if (arguments.size() > 1 && arguments.get(0).equals("help"))
                    return Collections.emptyList();

                boolean containsServerType = arguments.stream().anyMatch((arg) -> {
                    List<String> incompatibilities = new ArrayList<>(serverTypes);
                    incompatibilities.add("group");
                    return incompatibilities.contains(arg);
                });

                boolean containsGroup = arguments.stream().anyMatch((arg) -> {
                    List<String> incompatibilities = new ArrayList<>();
                    incompatibilities.add("sort");
                    incompatibilities.add("group");
                    return incompatibilities.contains(arg);
                });

                List<String> possibleArguments = new ArrayList<>();

                if (!containsServerType) {
                    possibleArguments.addAll(serverTypes);
                    if (!containsGroup) {
                        possibleArguments.add("group");
                    }
                }
                if (!containsGroup) {
                    possibleArguments.add("sort");
                }
                possibleArguments.add("count");
                if (arguments.size() == 1) {
                    possibleArguments.add("help");
                }
                possibleArguments.removeAll(arguments);

                return getListOfStringsMatchingLastWord(args, possibleArguments);
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
