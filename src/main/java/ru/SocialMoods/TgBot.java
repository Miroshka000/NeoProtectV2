package ru.SocialMoods;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;

public class TgBot {
    private TelegramBot bot;
    private NeoProtect plugin;

    public TgBot(NeoProtect plugin) {
        this.plugin = plugin;
        String botToken = plugin.config.getString("bot-token");
        if (!botToken.isEmpty()) {
            this.bot = new TelegramBot(botToken);
        } else {
            plugin.getLogger().error(plugin.config.getString("messages.bot-token-error", "Установите токен для бота в конфиге!"));
        }

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    handleMessage(update);
                } else if (update.callbackQuery() != null) {
                    handleCallbackQuery(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleMessage(Update update) {
        String messageText = update.message().text();
        Long chatId = update.message().chat().id();

        if ("/start".equals(messageText)) {
            String startMessage = plugin.config.getString("messages.start-message", "Привет! Нажми на инлайн-кнопку!");
            String buttonText = plugin.config.getString("messages.start-button-text", "Туториал");
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(new InlineKeyboardButton(buttonText).callbackData("tutorial"));
            sendMessageWithKeyboard(chatId, startMessage, inlineKeyboard);
        } else if (messageText.startsWith("/verify ")) {
            String code = messageText.split(" ")[1];
            plugin.verifyTelegramCode(code, chatId);
            sendMessage(chatId, plugin.config.getString("messages.verification-success", "Верификация завершена."));
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.callbackQuery().data();
        Long chatId = update.callbackQuery().message().chat().id();
        String callbackId = update.callbackQuery().id();

        if ("tutorial".equals(callbackData)) {
            sendMessage(chatId, plugin.config.getString("messages.tuturial", "Настройте текст туториала в конфиге!"));
        }

        bot.execute(new AnswerCallbackQuery(callbackId));
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage request = new SendMessage(chatId, message);
        bot.execute(request);
    }

    public void sendMessageWithKeyboard(Long chatId, String message, InlineKeyboardMarkup keyboard) {
        SendMessage request = new SendMessage(chatId, message).replyMarkup(keyboard);
        bot.execute(request);
    }
}