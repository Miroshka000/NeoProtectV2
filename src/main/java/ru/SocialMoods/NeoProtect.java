package ru.SocialMoods;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.SocialMoods.Form.RegionFormManager;
import ru.SocialMoods.Storage.Areas;
import ru.SocialMoods.Storage.Stream;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NeoProtect extends PluginBase implements Listener {

    private HashMap<String, List<Areas>> regionMap;
    protected Stream stream;
    public Config config;
    private Map<String, VerificationCode> verificationCodes;
    private TgBot tgBot;
    private EventListener listener;
    private RegionFormManager formManager;
    private static final long VERIFICATION_EXPIRY_MS = TimeUnit.MINUTES.toMillis(10);

    @Override
    public void onEnable() {
        printPluginInfo();
        initializeConfig();
        initializeComponents();
        registerEvents();
        setupAutoSave();
    }
    
    private void printPluginInfo() {
        getLogger().info(TextFormat.AQUA + "-----------------------------------");
        getLogger().info(TextFormat.GREEN + "NeoProtect " + TextFormat.YELLOW + "v" + getDescription().getVersion() + TextFormat.GREEN + " успешно запущен!");
        getLogger().info(TextFormat.GREEN + "Переработано ForgePlugins Studio");
        getLogger().info(TextFormat.AQUA + "-----------------------------------");
        getLogger().info(TextFormat.YELLOW + "Подпишитесь на наши ресурсы:");
        getLogger().info(TextFormat.BLUE + "• Telegram: " + TextFormat.WHITE + "https://t.me/ForgePlugins");
        getLogger().info(TextFormat.BLUE + "• VKontakte: " + TextFormat.WHITE + "https://vk.com/forgeplugin");
        getLogger().info(TextFormat.BLUE + "• GitHub: " + TextFormat.WHITE + "https://github.com/Miroshka000/NeoProtect");
        getLogger().info(TextFormat.AQUA + "-----------------------------------");
    }

    @Override
    public void onDisable() {
        if (stream != null) {
            stream.save();
        }
    }

    private void initializeConfig() {
        getDataFolder().mkdirs();
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            config = new Config(configFile, Config.YAML, createDefaultConfig());
        } else {
            config = new Config(configFile, Config.YAML);
        }
        config.save();
    }

    private void initializeComponents() {
        regionMap = new HashMap<>();
        verificationCodes = new HashMap<>();
        
        stream = new Stream(this);
        stream.init();
        
        initializeTelegramBot();
        
        listener = new EventListener(this);
        formManager = new RegionFormManager(this);
    }
    
    private void initializeTelegramBot() {
        String botToken = config.getString("bot.token", "");
        String botUsername = config.getString("bot.username", "");
        
        if (botToken.isEmpty()) {
            getLogger().warning("Токен для Telegram бота не указан в конфиге!");
            return;
        }
        
        try {
            tgBot = new TgBot(this);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(tgBot);
            getLogger().info("Telegram бот успешно запущен!");
        } catch (TelegramApiException e) {
            getLogger().error("Ошибка при запуске Telegram бота: " + e.getMessage(), e);
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void setupAutoSave() {
        new NukkitRunnable() {
            @Override
            public void run() {
                stream.save();
            }
        }.runTaskTimer(this, 6000, 6000);
    }

    private LinkedHashMap<String, Object> createDefaultConfig() {
        LinkedHashMap<String, Object> config = new LinkedHashMap<>();
        
        LinkedHashMap<String, Integer> protectionBlocks = new LinkedHashMap<>();
        protectionBlocks.put("22", 10);
        protectionBlocks.put("57", 15);
        config.put("protection-blocks", protectionBlocks);
        
        config.put("maximum-protections", 5);
        config.put("particles-enabled", true);
        
        LinkedHashMap<String, String> messages = new LinkedHashMap<>();
        messages.put("explosion-detected", "Взрыв произошел в вашем регионе. Координаты: {x}, {y}, {z}");
        messages.put("block-use-denied", "Вы не можете использовать: %block_name% в этой области.");
        messages.put("block-break-own", "Вы сломали свой защитный блок!");
        messages.put("block-break-denied", "Вы не можете сломать этот защитный блок!");
        messages.put("max-protections-reached", "Вы уже установили слишком много защитных блоков! Удалите некоторые, чтобы установить новые.");
        messages.put("protection-overlap", "Ваша защита пересекается с другой защищенной областью, отойдите подальше.");
        messages.put("protection-placed", "Вы установили защитный блок!");
        messages.put("protections-remaining", "У вас осталось %remaining% защитных областей.");
        messages.put("protection-radius-created", "Радиус защиты %radius% блоков создан вокруг вашего защитного блока.");
        messages.put("area-protected-popup", "Область внутри стеклянного круга защищена.");
        messages.put("area-owned-popup", "Вы находитесь в защищенной области, принадлежащей: %owner%");
        messages.put("player-added", "Игрок %player% добавлен в регион.");
        messages.put("player-removed", "Игрок %player% удален из региона.");
        messages.put("ownership-transferred", "Вы передали регион игроку %new_owner%.");
        messages.put("new-owner", "Вы стали владельцем региона %region%.");
        config.put("messages", messages);
        
        LinkedHashMap<String, Object> botConfig = new LinkedHashMap<>();
        botConfig.put("token", "");
        botConfig.put("username", "");
        
        LinkedHashMap<String, String> botMessages = new LinkedHashMap<>();
        botMessages.put("bot-enabled", "true");
        botMessages.put("bot-token-error", "Установите токен для бота в конфиге!");
        botMessages.put("start-message", "Привет! Ты в телеграмм боте плагина NeoProtect. Данный плагин позволяет получать уведомления о рейдах на приват.");
        botMessages.put("start-button-text", "Как подключить?");
        botMessages.put("start-chat-id", "Ваш айди чата: {chat_id}");
        botMessages.put("verification-success", "Верификация завершена.");
        botMessages.put("tutorial", "Для подключения аккаунта введите команду /verify на сервере!");
        botMessages.put("verify-code", "Ваш код для верификации: /verify ");
        botConfig.put("messages", botMessages);
        botConfig.put("verifications", new LinkedHashMap<String, String>());
        config.put("bot", botConfig);
        
        LinkedHashMap<String, Boolean> notifications = new LinkedHashMap<>();
        notifications.put("PlayerName", false);
        config.put("notifications", notifications);
        
        LinkedHashMap<String, String> formTexts = new LinkedHashMap<>();
        formTexts.put("region-management-title", "Управление регионом");
        formTexts.put("add-player-title", "Добавить игрока");
        formTexts.put("add-player-placeholder", "Введите имя игрока");
        formTexts.put("remove-player-title", "Удалить игрока");
        formTexts.put("remove-player-placeholder", "Введите имя игрока");
        formTexts.put("transfer-ownership-title", "Передать владение");
        formTexts.put("transfer-ownership-placeholder", "Введите имя нового владельца");
        formTexts.put("notification-settings-title", "Настройки уведомлений");
        formTexts.put("notification-toggle", "Получать уведомления в Telegram");
        formTexts.put("save-settings-button", "Сохранить настройки");
        formTexts.put("close-button", "Закрыть");
        config.put("form", formTexts);
        
        return config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "verify":
                return handleVerifyCommand(sender);
            case "protect":
                return handleProtectCommand(sender);
            default:
                return false;
        }
    }
    
    private boolean handleVerifyCommand(CommandSender sender) {
        if (!sender.isPlayer()) {
            sender.sendMessage(TextFormat.RED + "Эту команду может выполнить только игрок.");
            return false;
        }
        
        String playerName = sender.getName();
        String code = generateVerificationCode();
        verificationCodes.put(code, new VerificationCode(playerName, System.currentTimeMillis() + VERIFICATION_EXPIRY_MS));
        sender.sendMessage(config.getString("bot.messages.verify-code", "Ваш код для верификации: /verify ") + code);
        return true;
    }
    
    private boolean handleProtectCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return false;
        }
        
        checkPlayerRegion((Player) sender);
        return true;
    }

    public void toggleTelegramNotifications(Player player) {
        String playerName = player.getName();
        boolean currentSetting = config.getBoolean("notifications." + playerName, false);
        config.set("notifications." + playerName, !currentSetting);
        config.save();

        String message = !currentSetting
                ? "Настройки Telegram-уведомлений включены."
                : "Настройки Telegram-уведомлений отключены.";

        player.sendMessage(TextFormat.GREEN + message);
    }

    public void verifyTelegramCode(String code, Long chatId) {
        VerificationCode verification = verificationCodes.get(code);
        if (verification != null && verification.isValid()) {
            String playerName = verification.getPlayerName();
            config.set("bot.verifications." + playerName, chatId);
            config.save();
            verificationCodes.remove(code);
            getLogger().info("Игрок " + playerName + " успешно привязан к чату " + chatId);
        } else {
            getLogger().info("Неверный или просроченный код верификации: " + code);
        }
    }

    public Long getChatIdByPlayerName(String playerName) {
        return config.getLong("bot.verifications." + playerName);
    }

    public String getPlayerNameByChatId(Long chatId) {
        for (String playerName : config.getSection("bot.verifications").getKeys(false)) {
            Long savedChatId = config.getLong("bot.verifications." + playerName);
            if (savedChatId.equals(chatId)) {
                return playerName;
            }
        }
        return null;
    }

    public TgBot getTgBot() {
        return tgBot;
    }
    
    public EventListener getListener() {
        return listener;
    }
    
    public HashMap<String, List<Areas>> getRegionMap() {
        return regionMap;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    public void checkPlayerRegion(Player player) {
        Location playerLocation = player.getLocation();
        for (Map.Entry<String, List<Areas>> entry : regionMap.entrySet()) {
            String owner = entry.getKey();
            List<Areas> areas = entry.getValue();
            for (Areas area : areas) {
                Location areaLocation = area.getLocation();
                if (areaLocation != null && areaLocation.getLevel().equals(playerLocation.getLevel())) {
                    int radius = listener.getProtectionRadius(area.getBlockId());
                    if (playerLocation.distance(areaLocation) < radius) {
                        if (owner.equals(player.getName())) {
                            formManager.sendRegionManagementForm(player, area);
                        } else {
                            player.sendMessage("Вы не являетесь владельцем этого региона.");
                        }
                        return;
                    }
                }
            }
        }
        player.sendMessage("Рядом с вами нет регионов.");
    }

    public String getConfigText(String path, String defaultValue) {
        return config.getString(path, defaultValue);
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
}