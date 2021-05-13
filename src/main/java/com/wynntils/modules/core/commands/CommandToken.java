/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.commands;

import com.wynntils.webapi.WebManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

public class CommandToken extends CommandBase implements IClientCommand {


    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "token";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Returns your Wynntils auth token";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (WebManager.getAccount().getToken() != null) {
            StringTextComponent text = new StringTextComponent("");
            text.append("Wynntils Token ");
            text.getStyle().withColor(TextFormatting.AQUA);

            StringTextComponent token = new StringTextComponent(WebManager.getAccount().getToken());

            token.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://account.wynntils.com/register.php?token=" + WebManager.getAccount().getToken()));
            token.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new StringTextComponent("Click me to register an account.")));

            token.getStyle().withColor(TextFormatting.DARK_AQUA);
            token.getStyle().setUnderlined(true);
            text.append(token);

            sender.sendMessage(text);
            return;
        }

        StringTextComponent text = new StringTextComponent("Error when getting token, try restarting your client");
        text.getStyle().withColor(TextFormatting.RED);

        sender.sendMessage(text);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

}
