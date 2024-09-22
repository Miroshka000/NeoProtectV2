package ru.SocialMoods.Storage;

import ru.SocialMoods.NeoProtect;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public class Stream {
    
    private NeoProtect plugin;
    private String dataFile = "block.dat";
    
    public Stream(NeoProtect plugin) {
        this.plugin = plugin;
    }
    
    public void init() {
        try {
            plugin.getLogger().info("Сохранения приватов...");
            FileInputStream fis = new FileInputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            plugin.map = (HashMap) ois.readObject();
            ois.close();
            plugin.getLogger().info("Данные приватов сохранены!");
        } catch (FileNotFoundException e) {
            plugin.getLogger().info("Данные приватов не найдены!");
            plugin.map = new HashMap();
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().critical("Произошла ошибка: " + e.getMessage());
            plugin.map = new HashMap();
        }
    }
    
    public void save() {
        try {
            plugin.getLogger().info("Сохранения приватов..");
            FileOutputStream fos = new FileOutputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(plugin.map);
            oos.close();
            plugin.getLogger().info("Данные приватов сохранены!");
        } catch (IOException e) {
            plugin.getLogger().critical("Произошла ошибка: " + e.getMessage());
        }
    }
    
    
    
}
