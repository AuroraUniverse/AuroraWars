package ru.etyosft.aurorawars.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.etysoft.aurorauniverse.data.Towns;
import ru.etysoft.aurorauniverse.world.Town;

import java.util.ArrayList;
import java.util.List;

public class MainTabCompleter implements org.bukkit.command.TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0) {
            ArrayList<String> completions = new ArrayList<>();
            completions.add("info");
            completions.add("placeflag");


            if (sender.hasPermission("aurorawars.admin"))
            {
                completions.add("declare");
                completions.add("fend");
                completions.add("end");

                for(Town town : Towns.getTowns())
                {
                    completions.add(town.getName());
                }
            }
            ArrayList<String> finalCompletions = new ArrayList<>();
            for (String completion : completions) {
                if (completion.contains(args[args.length - 1].toLowerCase())) {
                   finalCompletions.add(completion);
                }
            }

            return finalCompletions;
        } else {
            return null;
        }
    }
}
