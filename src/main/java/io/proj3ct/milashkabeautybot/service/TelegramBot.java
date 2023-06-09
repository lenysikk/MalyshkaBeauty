package io.proj3ct.milashkabeautybot.service;

import io.proj3ct.milashkabeautybot.config.BotConfig;
import io.proj3ct.milashkabeautybot.model.*;
import io.proj3ct.milashkabeautybot.model.User;
import io.proj3ct.milashkabeautybot.repositories.*;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.*;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdsRepository adsRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private MasterRepository masterRepository;
    @Autowired
    private RecordsRepository recordsRepository;
    @Autowired
    private StandardTextResponseRepository standardTextResponseRepository;
    final BotConfig config;
    @Autowired
    private EmailService emailService;
    final List<BotCommand> listOfCommands = new ArrayList<>();
    private Map<Long, StateReg> statesRegistration = new HashMap<>();
    private Map<Long, StateRecord> statesRecord = new HashMap<>();
    private Map<Long, User> userRegistrationData = new HashMap<>();
    private Map<Long, Records> userRecordData = new HashMap<>();
    private String helpText = "${help_text}";
    private String errorText = "${error_text}";
    private String scheduleText = "${schedule_text}";

    private String infoText = "${info_text}";

    private void updateInfoText() {
        if (standardTextResponseRepository.findByName("INFO") != null) {
            this.infoText = standardTextResponseRepository.findByName("INFO").getText();
        }
    }

    private void updateScheduleText() {
        if (standardTextResponseRepository.findByName("SCHEDULE") != null) {
            this.scheduleText = standardTextResponseRepository.findByName("SCHEDULE").getText();
        }
    }

    private void updateHelpText() {
        if (standardTextResponseRepository.findByName("HELP") != null) {
            this.helpText = standardTextResponseRepository.findByName("HELP").getText();
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
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    if (userRepository.findById(update.getMessage().getChatId()).isEmpty()) {
                        sendMenuMessage(chatId, "Введите ваше имя: ");
                        statesRegistration.put(chatId, StateReg.NAME);
                    }
                    break;
                case "/help":
                    updateHelpText();
                    sendMenuMessage(chatId, helpText);
                    break;
                case "Записаться на услугу":
                    handleRecordServiceCommand(chatId);
                    break;
                case "/data":
                    updateInfoText();
                    sendStandartMessage(chatId, infoText);
                    break;
                case "Получить расписание":
                    updateScheduleText();
                    sendStandartMessage(chatId, scheduleText);
                    break;
                case "/myentries":
                    displayAllMyEntries(chatId);
                    break;
                default:
                    handleUserInput(update.getMessage());
            }
        }
    }

    private void handleUserInput(Message message) {
        StateReg stateReg = statesRegistration.get(message.getChatId());
        StateRecord stateRecord = statesRecord.get(message.getChatId());
        if (stateReg != null) {
            registerUser(message, stateReg);
        } else if (stateRecord != null) {
            recordUser(message, stateRecord);
        } else {
            sendMenuMessage(message.getChatId(), "Sorry, command was not recognized");
        }
    }

    private void handleRecordServiceCommand(Long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent() && optionalUser.get().isAccess()) {
            displayAllServiceName(chatId);
            statesRecord.put(chatId, StateRecord.SERVICE_TYPE);
        } else {
            sendStandartMessage(chatId, "Сейчас вы не можете это сделать - пожалуйста зарегистрируйтесь.\nВведите ваше имя:");
            statesRegistration.put(chatId, StateReg.NAME);
        }
    }

    private void displayAllMyEntries(long chatId) {
        List<Records> userRecords = recordsRepository.findAllByUserId(chatId);
        if (!userRecords.isEmpty()) {
            for (Records userRecord : userRecords) {
                sendStandartMessage(chatId,
                        userRecord.getService().getName() + " "
                                + userRecord.getMaster().getName() + " "
                                + userRecord.getComment() + "\n");
            }
        } else {
            sendStandartMessage(chatId, "У вас сейчас нет подтвержденных записей");
        }
    }


    private void registerUser(Message message, StateReg stateReg) {
        switch (stateReg) {
            case NAME:
                processNameInput(message);
                break;
            case EMAIL:
                processEmailInput(message);
                break;
            case PHONE_NUMBER:
                processPhoneNumberInput(message);
                break;
            case ACCESS_CODE:
                processAccessCodeInputRegistration(message);
                break;
            default:
                sendMenuMessage(message.getChatId(), "Sorry, command was not recognize");
                log.info(message.getText());
                break;
        }
    }

    private void processNameInput(Message message) {
        String name = message.getText();
        long chatId = message.getChatId();
        Chat chat = message.getChat();
        User user = new User();
        user.setId(chatId);
        user.setUserName(chat.getUserName());
        user.setRegisterAt(new Timestamp(System.currentTimeMillis()));
        user.setName(name);
        statesRegistration.put(chatId, StateReg.EMAIL);
        userRegistrationData.put(chatId, user);
        sendMenuMessage(chatId, "Теперь введите вашу почту: ");
    }

    private void processEmailInput(Message message) {
        String email = message.getText();
        long chatId = message.getChatId();
        statesRegistration.put(chatId, StateReg.PHONE_NUMBER);
        userRegistrationData.get(chatId).setEmail(email);
        sendMenuMessage(chatId, "Теперь введите номер телефона: ");
    }

    private void processPhoneNumberInput(Message message) {
        String phoneNumber = message.getText();
        long chatId = message.getChatId();
        statesRegistration.put(chatId, StateReg.ACCESS_CODE);
        userRegistrationData.get(chatId).setPhoneNumber(phoneNumber);
        sendMenuMessage(chatId, "Код с подтверждением регистрации придет на почту. Ваш код:");
        userRegistrationData.get(chatId).setAccessCode(generateCode());
        emailService.sendEmail(userRegistrationData.get(chatId).getEmail(), "Подтверждение регистрации: ",
                String.valueOf(userRegistrationData.get(chatId).getAccessCode()));
        log.info("user saved " + userRegistrationData.get(chatId).toString());
    }

    private void processAccessCodeInputRegistration(Message message) {
        long chatId = message.getChatId();
        String accessCode = message.getText();
        if (checkAccessCodeRegistration(chatId, accessCode)) {
            sendMenuMessage(chatId, "Регистрация прошла успешно!");
            userRegistrationData.get(chatId).setAccess(true);
            statesRegistration.remove(chatId);
            userRepository.save(userRegistrationData.get(chatId));
            userRegistrationData.remove(chatId);
        } else {
            sendMenuMessage(chatId, "Код невалидный, попробуйте еще раз");
        }
    }

    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }

        return code.toString();
    }


    private boolean checkAccessCodeRegistration(Long chatId, String code) {
        if (code.equals(String.valueOf(userRegistrationData.get(chatId).getAccessCode()))) {
            log.info("user has valid access code: " + chatId);
            return true;
        }
        return false;
    }
    private void recordUser(Message message, StateRecord stateRecord) {
        switch (stateRecord) {
            case SERVICE_TYPE:
                processServiceTypeInput(message);
                break;
            case MASTER:
                processMasterInput(message);
                break;
            case COMMENT:
                processCommentInput(message);
                break;
            case ACCESS_CODE:
                processAccessCodeInputRecord(message);
                break;
            default:
                sendMenuMessage(message.getChatId(), "Sorry, command was not recognized");
                log.info(message.getText());
                break;
        }
    }

    private void processAccessCodeInputRecord(Message message) {
        long chatId = message.getChatId();
        String accessCode = message.getText();
        Optional<Records> optionalRecords = recordsRepository.findById(chatId);
        if (checkAccessCodeRecord(chatId, accessCode) && optionalRecords.isPresent()) {
            sendMenuMessage(chatId, "Запись прошла успешно!");
            userRecordData.get(chatId).setAccess(true);
            statesRecord.remove(chatId);
            optionalRecords.get().setAccess(true);
            userRecordData.remove(chatId);
        } else {
            sendMenuMessage(chatId, "Код невалидный, попробуйте еще раз");
        }
    }

    private boolean checkAccessCodeRecord(Long chatId, String code) {
        if (code.equals(String.valueOf(userRecordData.get(chatId).getAccessCode()))) {
            log.info("user has valid access code: " + chatId);
            return true;
        }
        return false;
    }

    private void processServiceTypeInput(Message message) {
        long chatId = message.getChatId();
        String serviceTypeName = message.getText();
        statesRecord.put(chatId, StateRecord.MASTER);
        displayAllMasterName(chatId, serviceTypeName);
        Records records = new Records();
        records.setService(serviceRepository.findByName(serviceTypeName));
        records.setUser(userRepository.findById(chatId).orElse(null));
        userRecordData.put(chatId, records);
    }

    private void processMasterInput(Message message) {
        long chatId = message.getChatId();
        statesRecord.put(chatId, StateRecord.COMMENT);
        sendStandartMessage(chatId, "Оставьте комментарий для мастера");
        userRecordData.get(chatId).setMaster(masterRepository.findByName(message.getText()));
    }

    private void processCommentInput(Message message) {
        long chatId = message.getChatId();
        statesRecord.put(chatId, StateRecord.ACCESS_CODE);
        userRecordData.get(chatId).setComment(message.getText());
        Records records = userRecordData.get(chatId);
        recordsRepository.save(records);
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            sendMenuMessage(chatId, "Код с подтверждением записи придет на почту: " + user.getEmail() + " Ваш код:");
            userRecordData.get(chatId).setAccessCode(generateCode());
            emailService.sendEmail(user.getEmail(), "Подтверждение регистрации: ",
                    String.valueOf(userRecordData.get(chatId).getAccessCode()));
        }
    }


    private void displayAllMasterName(Long chatId, String serviceName) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите доступного мастера:");
        List<Master> masters = masterRepository.findByServicesName(serviceName);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (Master master : masters) {
            keyboardRow.add(master.getName());
        }
        keyboardRows.add(keyboardRow);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void displayAllServiceName(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите доступную услугу:");
        var services = serviceRepository.findAll();

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (Service service : services) {
            keyboardRow.add(service.getName());
        }
        keyboardRows.add(keyboardRow);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String greeting = "Здравствуйте, " + name + "!\n";
        String commands = "Список доступных команд:\n" + getListOfCommands();

        //реализовать вывод информации о салоне красоты

        log.info("Hello to user " + name);
        sendMenuMessage(chatId, greeting + commands);
    }

    private String getListOfCommands() {
        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i < listOfCommands.size(); ++i) {
            answerBuilder.append("Введите ")
                    .append(listOfCommands.get(i).getCommand())
                    .append(", чтобы ")
                    .append(listOfCommands.get(i).getDescription())
                    .append("\n");
        }
        return answerBuilder.toString();
    }

    private void sendMenuMessage(long chatId, String textToSend) {
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

    private void sendStandartMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(errorText + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds() {
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();

        for (Ads ad : ads) {
            for (User user : users) {
                prepareAndSendMessage(user.getId(), ad.getAd());
            }
        }
    }
}
