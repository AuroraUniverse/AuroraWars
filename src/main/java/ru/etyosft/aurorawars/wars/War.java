package ru.etyosft.aurorawars.wars;


import org.bukkit.Bukkit;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.events.WarEndedEvent;
import ru.etyosft.aurorawars.exceptions.AlreadyInWarException;
import ru.etyosft.aurorawars.exceptions.TownNotFoundedException;
import ru.etyosft.aurorawars.exceptions.WarEndedException;
import ru.etyosft.aurorawars.gui.WinnerMenu;
import ru.etysoft.aurorauniverse.chat.AuroraChat;
import ru.etysoft.aurorauniverse.world.Town;

public class War {

    private Town attacker;
    private Town victim;
    private boolean isEnded = false;

    private int attackerPoints;
    private int victimPoints;

    private long timestamp;

    public War(Town attacker, Town victim) throws AlreadyInWarException
    {
        if(AuroraWars.hasWar(attacker) | AuroraWars.hasWar(victim) | attacker == victim)
        {
            System.out.println(AuroraWars.hasWar(attacker) + " " + AuroraWars.hasWar(victim) + " " +(attacker == victim) + " - ?");
            throw new AlreadyInWarException();
        }
        else
        {
        attackerPoints = attacker.getResidents().size();
        victimPoints = victim.getResidents().size();
        this.victim = victim;
        this.attacker = attacker;
        victim.setForcePvp(true);
        attacker.setForcePvp(true);
        AuroraWars.removeAllRequests(attacker);
        AuroraWars.removeAllRequests(victim);
        AuroraWars.addWar(this);
        timestamp = System.currentTimeMillis();
        }
    }

    public void checkExpired()
    {
        if(System.currentTimeMillis() - timestamp > ((long) AuroraWars.getConfigFile().getIntFromConfig("war.max-time-sec") * 1000) && !isEnded)
        {
            AuroraChat.sendGlobalMessage(AuroraWars.getConfigFile().getStringFromConfig("war.expired")
                    .replace("%attacker%", attacker.getName())
            .replace("%victim%", victim.getName()));
            try
            {
                if(victimPoints > attackerPoints)
                {
                    attackerPoints = 0;
                    end(true);
                }
                else if(victimPoints < attackerPoints)
                {
                    victimPoints = 0;

                    end(true);
                }
                else
                {
                    end(false);
                    AuroraChat.sendGlobalMessage(AuroraWars.getConfigFile().getStringFromConfig("war.draw")
                            .replace("%attacker%", attacker.getName())
                            .replace("%victim%", victim.getName()));
                }

            }
            catch (Exception ignored) {}
        }
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public int getVictimPoints() {
        return victimPoints;
    }

    public Town getAttacker() {
        return attacker;
    }

    public Town getVictim() {
        return victim;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public int getHashCode()
    {
        return victim.getId().hashCode() * attacker.getId().hashCode();
    }

    public void end() throws WarEndedException {
        end(true);
    }

    public void end(boolean withForce) throws WarEndedException {
        if(isEnded) throw new WarEndedException();
        isEnded = true;
        AuroraWars.remove(this);
        Bukkit.getServer().getPluginManager().callEvent(new WarEndedEvent(this));

        victim.setForcePvp(false);
        attacker.setForcePvp(false);

        Town winner;
        Town looser;

        if(attackerPoints <= 0)
        {
            winner = victim;
            looser = attacker;
        }
        else
        {
            winner = attacker;
            looser = victim;
        }

        String declare = AuroraWars.getConfigFile().getStringFromConfig("war.win")
                .replace("%winner%", winner.getName())
                .replace("%looser%", looser.getName());

        AuroraChat.sendGlobalMessage(declare);
        if(withForce)
        {
            WinnerMenu winnerMenu = new WinnerMenu(winner, looser);
            AuroraWars.winnerWait.put(winner, winnerMenu);
            winnerMenu.open(winner.getMayor().getPlayer());
        }
    }



    public Town addPoints(Town t, int points) throws WarEndedException, TownNotFoundedException {
        if(t == attacker)
        {
            addAttacker(points);
            return attacker;
        }
        else if(t == victim)
        {
            addVictim(points);
            return victim;
        }
        throw new TownNotFoundedException();
    }

    public void addAttacker(int points) throws WarEndedException {

        if(isEnded) throw new WarEndedException();

        attackerPoints += points;
        victimPoints -= points;

        if (victimPoints <= 0)
        {
            end();
        }
        checkExpired();
    }

    public void addVictim(int points) throws WarEndedException {

        if(isEnded) throw new WarEndedException();
        victimPoints += points;
        attackerPoints -= points;

        if (attackerPoints <= 0)
        {
            end();
        }
        checkExpired();
    }
}
