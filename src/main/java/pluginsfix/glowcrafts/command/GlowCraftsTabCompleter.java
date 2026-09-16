package pluginsfix.glowcrafts.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlowCraftsTabCompleter implements TabCompleter {

    private static final List<String> ROOT_SUBS = List.of("create", "list", "reload");
    private static final List<String> CREATE_TYPES = List.of("shaped", "shapeless", "anvil", "furnace");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("glowcrafts.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ROOT_SUBS, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("glowcrafts.admin.create")) {
                return Collections.emptyList();
            }
            return StringUtil.copyPartialMatches(args[1], CREATE_TYPES, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
