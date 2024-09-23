package ru.SocialMoods.Form;

import cn.nukkit.Player;
import com.formconstructor.form.CustomForm;
import com.formconstructor.form.SimpleForm;
import com.formconstructor.form.element.custom.Input;
import com.formconstructor.form.element.custom.Toggle;
import ru.SocialMoods.EventListener;
import ru.SocialMoods.NeoProtect;
import ru.SocialMoods.Storage.Areas;

public class RegionFormManager {

    private final NeoProtect plugin;
    private final EventListener listener;

    public RegionFormManager(NeoProtect plugin) {
        this.plugin = plugin;
        this.listener = plugin.getListener();
    }

    public void sendRegionManagementForm(Player player, Areas region) {
        SimpleForm form = new SimpleForm(plugin.getConfigText("form.region-management-title", "Управление регионом"));

        form.addButton(plugin.getConfigText("form.add-player-title", "Добавить игрока"), (pl, b) -> sendAddPlayerForm(pl, region));
        form.addButton(plugin.getConfigText("form.remove-player-title", "Удалить игрока"), (pl, b) -> sendRemovePlayerForm(pl, region));
        form.addButton(plugin.getConfigText("form.transfer-ownership-title", "Передать владение"), (pl, b) -> sendTransferOwnershipForm(pl, region));
        form.addButton(plugin.getConfigText("form.notification-settings-title", "Настройки уведомлений"), (pl, b) -> sendNotificationSettingsForm(pl, region));

        form.setNoneHandler(pl -> {
        });

        form.send(player);
    }

    private void sendAddPlayerForm(Player player, Areas region) {
        CustomForm form = new CustomForm(plugin.getConfigText("form.add-player-title", "Добавить игрока"));
        form.addElement("input", new Input(plugin.getConfigText("form.add-player-placeholder", "Введите имя игрока")));

        form.setHandler((pl, response) -> {
            String playerName = response.getInput("input").getValue();
            region.addPlayer(playerName);
            pl.sendMessage("Игрок " + playerName + " добавлен в регион.");
        });

        form.setNoneHandler(pl -> sendRegionManagementForm(pl, region));

        form.send(player);
    }

    private void sendRemovePlayerForm(Player player, Areas region) {
        CustomForm form = new CustomForm(plugin.getConfigText("form.remove-player-title", "Удалить игрока"));
        form.addElement("input", new Input(plugin.getConfigText("form.remove-player-placeholder", "Введите имя игрока")));

        form.setHandler((pl, response) -> {
            String playerName = response.getInput("input").getValue();
            region.removePlayer(playerName);
            pl.sendMessage("Игрок " + playerName + " удален из региона.");
        });

        form.setNoneHandler(pl -> sendRegionManagementForm(pl, region));

        form.send(player);
    }

    private void sendTransferOwnershipForm(Player player, Areas region) {
        CustomForm form = new CustomForm(plugin.getConfigText("form.transfer-ownership-title", "Передать владение"));
        form.addElement("input", new Input(plugin.getConfigText("form.transfer-ownership-placeholder", "Введите имя нового владельца")));

        form.setHandler((pl, response) -> {
            String newOwnerName = response.getInput("input").getValue();
            listener.transferRegionOwnership(player.getName(), newOwnerName, region.getLocation());
            pl.sendMessage("Регион передан игроку " + newOwnerName + ".");
        });

        form.setNoneHandler(pl -> sendRegionManagementForm(pl, region));

        form.send(player);
    }

    private void sendNotificationSettingsForm(Player player, Areas region) {
        CustomForm form = new CustomForm(plugin.getConfigText("form.notification-settings-title", "Настройки уведомлений"));

        boolean notificationsEnabled = plugin.config.getBoolean("notifications." + player.getName(), false);
        form.addElement("toggle", new Toggle(plugin.getConfigText("form.notification-toggle", "Получать уведомления в Telegram")));

        form.setHandler((pl, response) -> {
            boolean notificationsSetting = response.getToggle("toggle").getValue();
            plugin.config.set("notifications." + player.getName(), notificationsSetting);
            plugin.config.save();
        });

        form.setNoneHandler(pl -> sendRegionManagementForm(pl, region));

        form.send(player);
    }
}
