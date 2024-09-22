package ru.SocialMoods;

import cn.nukkit.scheduler.PluginTask;

public class SaveTask extends PluginTask<NeoProtect> {
    private NeoProtect plugin;
    
    public SaveTask(NeoProtect owner) {
        super(owner);
        this.plugin = owner;
    }

    @Override
    public void onRun(int i) {
        plugin.stream.save();
    }
}
