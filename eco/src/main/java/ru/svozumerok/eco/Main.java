package ru.svozumerok.eco;

import org.bukkit.plugin.java.JavaPlugin;
//import EconomyCommand;
//import PlayerData; wait for ts

public class Main extends JavaPlugin {
    private PlayerData plrdata;

    @Override
    public void onEnable() {
        this.plrdata = new PlayerData(this);
        getcommand("exic").setExecutor(new EconomyCommand(this));
        getLogger.info("Fuck u svozumerok!")
    }
    
    @Override
    public void onDisable() {
        getLogger.info("fuck u svozumer!")
    }
    public PlayerData getplrdata() {
        return plrdata;
    }
}