package me.abobymotaki.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MissileCommand implements CommandExecutor {

    private Enchantments plugin;

    public MissileCommand(Enchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("missiles.use")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                plugin.launchMissile(target);
                return true;
            } else {
                sender.sendMessage("Player not found!");
                return false;
            }
        } else if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            plugin.launchMissile(player);
            return true;
        } else {
            sender.sendMessage("Usage: /missiles [player]");
            return false;
        }
    }
}
