package ru.SocialMoods;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class TgBot extends TelegramLongPollingBot {
    private final NeoProtect plugin;
    private final String botUsername;

    public TgBot(NeoProtect plugin) {
        super(plugin.config.getString("bot.token", ""));
        this.plugin = plugin;
        this.botUsername = plugin.config.getString("bot.username", "");
        
        if (getBotToken().isEmpty()) {
            plugin.getLogger().error(plugin.getConfig().getString("bot.messages.bot-token-error", "Установите токен для бота в конфиге!"));
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            plugin.getLogger().error("Error processing update: " + e.getMessage(), e);
        }
    }

    private void handleMessage(Message message) {
        String messageText = message.getText();
        Long chatId = message.getChatId();

        if ("/start".equals(messageText)) {
            String startMessage = plugin.config.getString("bot.messages.start-message", "Привет! Нажми на инлайн-кнопку!");
            String buttonText = plugin.config.getString("bot.messages.start-button-text", "Туториал");
            
            InlineKeyboardMarkup inlineKeyboard = createInlineKeyboard(buttonText, "tutorial");
            sendMessageWithKeyboard(chatId, startMessage, inlineKeyboard);
        } else if (messageText.startsWith("/verify ")) {
            String code = messageText.split(" ")[1];
            plugin.verifyTelegramCode(code, chatId);
            sendMessage(chatId, plugin.config.getString("bot.messages.verification-success", "Верификация завершена."));
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackId = callbackQuery.getId();

        if ("tutorial".equals(callbackData)) {
            sendMessage(chatId, plugin.config.getString("bot.messages.tutorial", "Настройте текст туториала в конфиге!"));
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackId)
                    .build();
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            plugin.getLogger().error("Error answering callback query: " + e.getMessage(), e);
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        
        return inlineKeyboardMarkup;
    }

    public void sendMessage(Long chatId, String text) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            plugin.getLogger().error("Error sending message: " + e.getMessage(), e);
        }
    }

    public void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            plugin.getLogger().error("Error sending message with keyboard: " + e.getMessage(), e);
        }
    }

    public boolean isNotificationsEnabled(Long chatId) {
        String playerName = plugin.getPlayerNameByChatId(chatId);
        return plugin.config.getBoolean("notifications." + playerName, false);
    }
}