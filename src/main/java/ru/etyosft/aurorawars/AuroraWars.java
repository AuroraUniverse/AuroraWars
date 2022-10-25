package ru.etyosft.aurorawars;



import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.etyosft.aurorawars.commands.MainExecutor;
import ru.etyosft.aurorawars.commands.MainTabCompleter;
import ru.etyosft.aurorawars.commands.Request;
import ru.etyosft.aurorawars.gui.WinnerMenu;
import ru.etyosft.aurorawars.listeners.MainListener;
import ru.etyosft.aurorawars.wars.War;
import ru.etysoft.aurorauniverse.data.Towns;
import ru.etysoft.aurorauniverse.world.Town;
import ru.etysoft.epcore.Console;
import ru.etysoft.epcore.EasyPluginCore;
import ru.etysoft.epcore.config.ConfigFile;

import java.util.ArrayList;
import java.util.HashMap;

public final class AuroraWars extends JavaPlugin {

    private static AuroraWars instance;
    private static ConfigFile configFile;

    private static HashMap<Town, Integer> townHashes = new HashMap<>();

    private static HashMap<String, ArrayList<Request>> requests = new HashMap<>();
    private static HashMap<Integer, War> wars = new HashMap<>();

    private static Thread warTimer;


    public static HashMap<Town, WinnerMenu> winnerWait = new HashMap<>();

    @Override
    public void onEnable() {
        if (EasyPluginCore.isSupported("0.4")) {
            Console.sendMessage("Starting &6AuroraWars...");
            instance = this;
            configFile = new ConfigFile(this);
            getCommand("twar").setExecutor(new MainExecutor());
            getCommand("twar").setTabCompleter(new MainTabCompleter());
            saveDefaultConfig();
            Bukkit.getPluginManager().registerEvents(new MainListener(), this);


            warTimer = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {
                        if(!isEnabled()) break;
                        try {
                            Thread.sleep(1000);
                            if(!isEnabled()) break;
                            for(War war : wars.values())
                            {
                                try {


                                if(war.getWarTimer() != null)
                                {
                                    war.getWarTimer().run();
                                }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            warTimer.start();
        } else {
            Bukkit.getLogger().warning("Unsupported EasyPluginCore API version detected! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

        Console.sendMessage("Disabling &6AuroraWars...");
    }

    public static void registerWar(War w) {
        wars.put(w.getHashCode(), w);
        townHashes.put(w.getAttacker(), w.getHashCode());
        townHashes.put(w.getVictim(), w.getHashCode());
    }

    public static ArrayList<Request> getWarRequests(Town recipient) {
        String key = recipient.getId().toString();
        if (requests.containsKey(key)) {
            ArrayList<Request> requestList = requests.get(key);
            ArrayList<Request> newRequestList = new ArrayList<>();
            for (Request req : requestList) {
                if (!req.isExpired()) {
                    newRequestList.add(req);
                }
            }
            requests.put(key, newRequestList);
            return requestList;
        } else {
            return new ArrayList<>();
        }
    }

    public static boolean sendRequest(Request request) {
        String key = request.getRecipient().getId().toString();
        if (requests.containsKey(key)) {
            ArrayList<Request> requestList = requests.get(key);

            ArrayList<Request> newRequestList = new ArrayList<>();
            for (Request req : requestList) {
                if (req.getSender() == request.getSender()) {
                    if (!req.isExpired()) {
                        newRequestList.add(req);

                    } else {
                        return false;
                    }

                }
            }
            newRequestList.add(request);
            requests.put(key, newRequestList);
            return true;

        } else {
            ArrayList<Request> requestList = new ArrayList<>();
            requestList.add(request);
            requests.put(key, requestList);
            return true;
        }
    }

    public static void removeAllRequests(Town town) {
        String key = town.getId().toString();
        if (requests.containsKey(key)) {
            ArrayList<Request> requestList = requests.get(key);
            ArrayList<Request> newRequestList = new ArrayList<>();
            for (Request req : requestList) {
                if (req.getRecipient() != town) {
                    newRequestList.add(req);
                }
            }
            requests.put(key, newRequestList);
        }

    }

    public static void removeRequest(Request request) {
        String key = request.getRecipient().getId().toString();
        if (requests.containsKey(key)) {
            ArrayList<Request> requestList = requests.get(key);
            requestList.remove(request);
            requests.put(key, requestList);
        }

    }

    public static void unregisterWar(War w) {
        wars.remove(w.hashCode());
        townHashes.remove(w.getAttacker());
        townHashes.remove(w.getVictim());
    }

    public static boolean hasWar(Town t) {
        return townHashes.containsKey(t);
    }

    public static War getWarForTown(Town t) {
        return wars.get(townHashes.get(t));
    }

    public static ConfigFile getConfigFile() {
        configFile = new ConfigFile(AuroraWars.getInstance());
        return configFile;
    }

    public static AuroraWars getInstance() {
        return instance;
    }
}
