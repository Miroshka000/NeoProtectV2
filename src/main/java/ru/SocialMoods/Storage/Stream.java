package ru.SocialMoods.Storage;

import ru.SocialMoods.NeoProtect;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Stream {

    private final NeoProtect plugin;
    private final String dataFile = "block.dat";

    public Stream(NeoProtect plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            plugin.getLogger().info("Сохранения приватов...");
            FileInputStream fis = new FileInputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            plugin.map = (HashMap<String, List<Areas>>) ois.readObject();
            ois.close();
            plugin.getLogger().info("Данные приватов сохранены!");
        } catch (FileNotFoundException e) {
            plugin.getLogger().info("Данные приватов не найдены!");
            plugin.map = new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().critical("Произошла ошибка: " + Arrays.toString(e.getStackTrace()));
            plugin.map = new HashMap<>();
        }
    }

    public void save() {
        try {
            plugin.getLogger().info("Сохранения приватов...");
            FileOutputStream fos = new FileOutputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(plugin.map);
            oos.close();
            plugin.getLogger().info("Данные приватов сохранены!");
        } catch (Exception e) {
            plugin.getLogger().critical("Произошла ошибка: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
