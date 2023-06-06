package io.proj3ct.MilashkaBeautyBot.service;

import io.proj3ct.MilashkaBeautyBot.config.BotConfig;
import io.proj3ct.MilashkaBeautyBot.model.Ads;
import io.proj3ct.MilashkaBeautyBot.model.AdsRepository;
import io.proj3ct.MilashkaBeautyBot.model.User;
import io.proj3ct.MilashkaBeautyBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdsRepository adsRepository;
    final BotConfig config;
    final List<BotCommand> listOfCommands = new ArrayList<>();
    static final String HELP_TEXT = "" +
            "По всем вопросом обращайтесь по адресу электронной почты: milashka@beauty.ru\n" +
            "Или по номеру телефона доступном по команде /data";

    static final String ERROR_TEXT = "Error occurred: ";

    private Map<Long, State> states = new HashMap<>();
    private Map<Long, User> userRegistrationData = new HashMap<>();
    enum State {
        START, NAME, PHONE_NUMBER, EMAIL
    }

    public TelegramBot(BotConfig config) {

        this.config = config;

        listOfCommands.add(new BotCommand("/start",
                "Начать работу."));
        listOfCommands.add(new BotCommand("/data",
                "Получить сведения о расписании, адресе, номере телефона салона красоты."));
        listOfCommands.add(new BotCommand("/myentries",
                "Получить мои записи на оказание услуг"));
        listOfCommands.add(new BotCommand("/help",
                "Обратиться в поддержку."));
        try{
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    if(userRepository.findById(update.getMessage().getChatId()).isEmpty()) {
                        sendMessage(chatId, "Введите ваше имя: ");
                        states.put(chatId, State.NAME);
                    }
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    State state = states.get(chatId);
                    if (state != null) {
                        registerUser(update.getMessage(), state);
                    } else {
                        sendMessage(chatId, "Sorry, command was not recognized");
                    }
            }
        }

    }

    private void registerUser(Message message, State state) {
        switch (state) {
            case NAME:
                String name = message.getText();
                var chatId = message.getChatId();
                var chat = message.getChat();
                User user = new User();
                user.setId(chatId);
                user.setUserName(chat.getUserName());
                user.setRegisterAt(new Timestamp(System.currentTimeMillis()));
                user.setName(name);
                states.put(message.getChatId(), State.EMAIL);
                userRegistrationData.put(message.getChatId(), user);
                sendMessage(message.getChatId(), "Теперь введите вашу почту: ");
                break;
            case EMAIL:
                String email = message.getText();
                states.put(message.getChatId(), State.PHONE_NUMBER);
                userRegistrationData.get(message.getChatId()).setEmail(email);
                sendMessage(message.getChatId(), "Теперь введите номер телефона:");
                break;
            case PHONE_NUMBER:
                String phoneNumber = message.getText();
                states.remove(message.getChatId());
                userRegistrationData.get(message.getChatId()).setPhoneNumber(phoneNumber);
                userRepository.save(userRegistrationData.get(message.getChatId()));
                userRegistrationData.remove(message.getChatId());
                log.info("user saved " + userRegistrationData.get(message.getChatId()).toString());
                sendMessage(message.getChatId(), "Регистрация завершена. Спасибо!");
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String greeting = "Здравствуйте, " + name + "!\n";
        String commands = "Список доступных команд:\n" + getListOfCommands();

        //реализовать вывод информации о салоне красоты

        log.info("Hello to user " + name);
        sendMessage(chatId, greeting + commands);
    }

    private String getListOfCommands() {
        String answer = "";
        for ( int i = 0; i < listOfCommands.size(); ++i) {
            answer+="Введите " + listOfCommands.get(i).getCommand() + ", чтобы " + listOfCommands.get(i).getDescription() + "\n";
        }
        return answer;
    }
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Записаться на услугу");
        keyboardRows.add(keyboardRow);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add("Получить расписание");
        keyboardRows.add(keyboardRow2);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds(){
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();

        for(Ads ad: ads) {
            for (User user: users) {
                prepareAndSendMessage(user.getId(), ad.getAd());
            }
        }
    }
}
