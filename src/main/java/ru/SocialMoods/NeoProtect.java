package ru.SocialMoods;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import ru.SocialMoods.Storage.Areas;
import ru.SocialMoods.Storage.Stream;

import java.io.File;
import java.util.*;

public class NeoProtect extends PluginBase implements Listener {

    public HashMap<String, List<Areas>> map;
    public Stream stream;
    public Config config;
    private Map<String, VerificationCode> verificationCodes;
    private TgBot tgBot;

    @Override
    public void onEnable() {
        getLogger().info("NeoProtect включен!");
        getLogger().info("Переработано SkyStudio");

        getDataFolder().mkdirs();
        
        File configFile = new File(this.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            config = new Config(configFile, Config.YAML, createDefaultConfig());
            config.save();
        } else {
            config = new Config(configFile, Config.YAML);
        }

        map = new HashMap<>();
        verificationCodes = new HashMap<>();

        stream = new Stream(this);
        stream.init();

        tgBot = new TgBot(this);

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    private LinkedHashMap<String, Object> createDefaultConfig() {
        return new LinkedHashMap<String, Object>() {
            {
                put("protection-blocks", new LinkedHashMap<String, Integer>() {
                    {
                        put("22", 10);
                        put("57", 15);
                    }
                });
                put("maximum-protections", 5);
                put("bot-token", "Вставьте свой токен телеграмм бота, если не вставите, возможен спам в консоли");
                put("messages", new LinkedHashMap<String, String>() {
                    {
                        put("explosion-detected", "Взрыв произошел в вашем регионе. Координаты: {x}, {y}, {z}");
                        put("block-use-denied", "Вы не можете использовать: %block_name% в этой области.");
                        put("block-break-own", "Вы сломали свой защитный блок!");
                        put("block-break-denied", "Вы не можете сломать этот защитный блок!");
                        put("max-protections-reached", "Вы уже установили слишком много защитных блоков! Удалите некоторые, чтобы установить новые.");
                        put("protection-overlap", "Ваша защита пересекается с другой защищенной областью, отойдите подальше.");
                        put("protection-placed", "Вы установили защитный блок!");
                        put("protections-remaining", "У вас осталось %remaining% защитных областей.");
                        put("protection-radius-created", "Радиус защиты %radius% блоков создан вокруг вашего защитного блока.");
                        put("area-protected-popup", "Область внутри стеклянного круга защищена.");
                        put("area-owned-popup", "Вы находитесь в защищенной области, принадлежащей: %owner%");
                        put("bot-token-error", "Установите токен для бота в конфиге!");
                        put("start-message", "Привет! Ты в телеграмм боте плагина NeoProtect. Данный плагин позволяет получать уведомления о рейдах на приват.");
                        put("start-button-text", "Как подключить?");
                        put("start-chat-id", "Ваш айди чата: {chat_id}");
                        put("verification-success", "Верификация завершена.");
                        put("tutorial", "Для подключения аккаунта введите команду /verify на сервере!");
                        put("verify-code", "Ваш код для верификации: /verify ");
                    }
                });
                put("verifications", new LinkedHashMap<String, String>());
            }
        };
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("verify")) {
            if (sender.isPlayer()) {
                String playerName = sender.getName();
                String code = generateVerificationCode();
                verificationCodes.put(code, new VerificationCode(playerName, System.currentTimeMillis() + 600000));
                sender.sendMessage(getConfig().getString("verify-code", "Ваш код для верификации: /verify ") + code);
                return true;
            } else {
                sender.sendMessage(TextFormat.RED + "Эту команду может выполнить только игрок.");
                return false;
            }
        }
        return false;
    }

    public void verifyTelegramCode(String code, Long chatId) {
        VerificationCode verification = verificationCodes.get(code);
        if (verification != null && verification.isValid()) {
            String playerName = verification.getPlayerName();
            config.set("verifications." + playerName, chatId);
            config.save();
            verificationCodes.remove(code);
            getLogger().info("Игрок " + playerName + " успешно привязан к чату " + chatId);
        } else {
            getLogger().info("Неверный или просроченный код верификации: " + code);
        }
    }

    public Long getChatIdByPlayerName(String playerName) {
        return config.getLong("verifications." + playerName);
    }

    public TgBot getTgBot() {
        return tgBot;
    }

    private static class VerificationCode {
        private final String playerName;
        private final long expiryTime;

        public VerificationCode(String playerName, long expiryTime) {
            this.playerName = playerName;
            this.expiryTime = expiryTime;
        }

        public String getPlayerName() {
            return playerName;
        }

        public boolean isValid() {
            return System.currentTimeMillis() < expiryTime;

        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }
}