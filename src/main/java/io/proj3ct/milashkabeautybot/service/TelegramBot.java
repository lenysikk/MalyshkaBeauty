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
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 6;
    final List<BotCommand> listOfCommands = new ArrayList<>();
    private Map<Long, StateReg> statesRegistration = new HashMap<>();
    private Map<Long, StateRecord> statesRecord = new HashMap<>();
    private Map<Long, User> userRegistrationData = new HashMap<>();
    private Map<Long, Records> userRecordData = new HashMap<>();
    private String helpText = "${help_text}";
    private String errorText = "${error_text}";
    private String scheduleText = "${schedule_text}";

    private String infoText = "${info_text}";

    private void updateInfoText() { // обновляем текст информации из базы данных
        if (standardTextResponseRepository.findByName("INFO") != null) {
            this.infoText = standardTextResponseRepository.findByName("INFO").getText();
        }
    }

    private void updateScheduleText() { // обновляем текст расаписания из базы данных
        if (standardTextResponseRepository.findByName("SCHEDULE") != null) {
            this.scheduleText = standardTextResponseRepository.findByName("SCHEDULE").getText();
        }
    }

    private void updateHelpText() { // обновляем текст поддержки из базы данных
        if (standardTextResponseRepository.findByName("HELP") != null) {
            this.helpText = standardTextResponseRepository.findByName("HELP").getText();
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    } // геттер имени бота

    @Override
    public String getBotToken() {
        return config.getToken();
    } // геттер токена бота

    public TelegramBot(BotConfig config) { // конструктор бота

        this.config = config;

        listOfCommands.add(new BotCommand("/start",
                "Начать работу."));
        listOfCommands.add(new BotCommand("/data",
                "Получить сведения о расписании, адресе, номере телефона салона красоты."));
        listOfCommands.add(new BotCommand("/myrecords",
                "Получить мои записи на оказание услуг"));
        listOfCommands.add(new BotCommand("/help",
                "Обратиться в поддержку."));
        try {
            // добавляем пользователю список команд для работы с ботом
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(errorText + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) { // по сути главный класс, получает обновления от пользователя
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName()); //приветственное сообщение
                    if (userRepository.findById(update.getMessage().getChatId()).isEmpty()) { // проверка на существовании в базе данных
                        sendMenuMessage(chatId, "Введите ваше имя: ");
                        statesRegistration.put(chatId, StateReg.NAME); // установили состояние регистрации
                    }
                    break;
                case "/help":
                    updateHelpText();  // получаем актуальный данные текста help из бд
                    sendMenuMessage(chatId, helpText);
                    break;
                case "Записаться на услугу":
                    // запускаем метод для записи и устанавливаем здесь состояние stateRecord
                    handleRecordServiceCommand(chatId);
                    break;
                case "/data":
                    updateInfoText(); // получаем актуальный данные текста info из бд
                    sendStandartMessage(chatId, infoText);
                    break;
                case "Получить расписание":
                    updateScheduleText(); // получаем актуальный данные текста расписания из бд
                    sendStandartMessage(chatId, scheduleText);
                    break;
                case "/myrecords":
                    displayAllMyEntries(chatId); // получаем список записей Recrods по id пользователя
                    break;
                default:
                    handleUserInput(update.getMessage()); // обрабатываем состояния (регистрации или записи )
            }
        }
    }

    private void handleUserInput(Message message) { // обрабатываем сообщение пользователя в зависимости от состояния
        StateReg stateReg = statesRegistration.get(message.getChatId());
        StateRecord stateRecord = statesRecord.get(message.getChatId());
        if (stateReg != null) {
            registerUser(message, stateReg); // рег пользователя
        } else if (stateRecord != null) {
            recordUser(message, stateRecord); // функция записи на услугу
        } else {
            sendMenuMessage(message.getChatId(), "Sorry, command was not recognized");
        }
    }

    private void handleRecordServiceCommand(Long chatId) { // метод для проверки регистрации пользователя, если да, то может записаться
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent() && optionalUser.get().isAccess()) {
            displayAllServiceName(chatId);
            statesRecord.put(chatId, StateRecord.SERVICE_TYPE); // установили состояние выбора услуги
        } else {
            sendStandartMessage(chatId, "Сейчас вы не можете это сделать - пожалуйста зарегистрируйтесь.\nВведите ваше имя:");
            statesRegistration.put(chatId, StateReg.NAME); // состояние регистрации
        }
    }

    private void displayAllMyEntries(long chatId) { // вывод списка записей конкретного пользователя по его id
        List<Records> userRecords = recordsRepository.findAllByUserId(chatId); // нашли по id пользователя его записи
        if (!userRecords.isEmpty()) { // если список не пуст вывели
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


    private void registerUser(Message message, StateReg stateReg) { // регистрация пользователя
        switch (stateReg) {
            case NAME:
                processNameInput(message); // записываем имя во временную data
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

    private void processNameInput(Message message) { // получаем имя, но не отправляем в базу данных, а записываем во временную
        String name = message.getText();
        long chatId = message.getChatId();
        Chat chat = message.getChat();
        User user = new User(); // новый юзер
        user.setId(chatId);
        user.setUserName(chat.getUserName());
        user.setRegisterAt(new Timestamp(System.currentTimeMillis()));
        user.setName(name);
        statesRegistration.put(chatId, StateReg.EMAIL); // следующее чего ждем эмаил
        userRegistrationData.put(chatId, user); // записываем пользвоателя с именем во временную data
        sendMenuMessage(chatId, "Теперь введите вашу почту: ");
    }

    private void processEmailInput(Message message) { // получаем email но пока храним во временной
        String email = message.getText();
        long chatId = message.getChatId();
        statesRegistration.put(chatId, StateReg.PHONE_NUMBER); // теперь ждем номер телефона
        userRegistrationData.get(chatId).setEmail(email);
        sendMenuMessage(chatId, "Теперь введите номер телефона: ");
    }

    private void processPhoneNumberInput(Message message) { // получает телефон но пока храним
        String phoneNumber = message.getText();
        long chatId = message.getChatId();
        userRegistrationData.get(chatId).setPhoneNumber(phoneNumber); // записываем во временную data
        sendMenuMessage(chatId, "Код с подтверждением регистрации придет на почту. Ваш код:");
        userRegistrationData.get(chatId).setAccessCode(generateCode()); // сгенерировали и записали код который будем ждать
        emailService.sendEmail(userRegistrationData.get(chatId).getEmail(), "Подтверждение регистрации: ",
                String.valueOf(userRegistrationData.get(chatId).getAccessCode()));
        statesRegistration.put(chatId, StateReg.ACCESS_CODE); // устанавливаем состояние ожидания кода
        log.info("user saved " + userRegistrationData.get(chatId).toString());
    }

    private void processAccessCodeInputRegistration(Message message) {
        long chatId = message.getChatId();
        String accessCode = message.getText();
        if (checkAccessCodeRegistration(chatId, accessCode)) { // проверка на правильность кода
            sendMenuMessage(chatId, "Регистрация прошла успешно!");
            userRegistrationData.get(chatId).setAccess(true);
            statesRegistration.remove(chatId);
            userRepository.save(userRegistrationData.get(chatId)); // сохраняем в базу
            userRegistrationData.remove(chatId); //убираем состояние регистрации вообще
        } else {
            sendMenuMessage(chatId, "Код невалидный, попробуйте еще раз");
        }
    }

    public static String generateCode() { // генерация кода для оптравки на почту пользователя
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }

        return code.toString();
    }


    private boolean checkAccessCodeRegistration(Long chatId, String code) { // проверка на правильность кода доступа
        if (code.equals(String.valueOf(userRegistrationData.get(chatId).getAccessCode()))) {
            log.info("user has valid access code: " + chatId);
            return true;
        }
        return false;
    }
    private void recordUser(Message message, StateRecord stateRecord) { // запись на услугу
        switch (stateRecord) {
            case SERVICE_TYPE:
                processServiceTypeInput(message); // получили название услуги
                break;
            case MASTER:
                processMasterInput(message); // получили имя мастера
                break;
            case COMMENT:
                processCommentInput(message); // получили комментарий и отправили код
                break;
            case ACCESS_CODE:
                processAccessCodeInputRecord(message); // получили код и очистили временные дата, и записали в бд
                break;
            default:
                sendMenuMessage(message.getChatId(), "Sorry, command was not recognized");
                log.info(message.getText());
                break;
        }
    }

    private void processAccessCodeInputRecord(Message message) { // проверка кода с почты и запись в базу данных
        long chatId = message.getChatId();
        String accessCode = message.getText();
        if (checkAccessCodeRecord(chatId, accessCode)) {
            sendMenuMessage(chatId, "Запись прошла успешно!");
            userRecordData.get(chatId).setAccess(true);
            statesRecord.remove(chatId);
            recordsRepository.save(userRecordData.get(chatId));
            userRecordData.remove(chatId);
        } else {
            sendMenuMessage(chatId, "Код невалидный, попробуйте еще раз");
        }
    }

    private boolean checkAccessCodeRecord(Long chatId, String code) { // проверка кода на идентичность с тем что ждали
        if (code.equals(String.valueOf(userRecordData.get(chatId).getAccessCode()))) {
            log.info("user has valid access code: " + chatId);
            return true;
        }
        return false;
    }

    private void processServiceTypeInput(Message message) { // получили услугу и установили состояние ожидания мастера
        long chatId = message.getChatId();
        String serviceTypeName = message.getText();
        statesRecord.put(chatId, StateRecord.MASTER); // состояние ожидания ввода пользователем мастера
        displayAllMasterName(chatId, serviceTypeName); // выводим всех мастеров для данной услуги
        Records records = new Records();
        records.setService(serviceRepository.findByName(serviceTypeName));
        records.setUser(userRepository.findById(chatId).orElse(null));
        userRecordData.put(chatId, records); // записали во временную дата
    }

    private void processMasterInput(Message message) { // получили мастера и ждем коммент теперь
        long chatId = message.getChatId();
        statesRecord.put(chatId, StateRecord.COMMENT);
        sendStandartMessage(chatId, "Оставьте комментарий для мастера");
        userRecordData.get(chatId).setMaster(masterRepository.findByName(message.getText()));
    }

    private void processCommentInput(Message message) { // поулчили коммент и отправили код на почту, ждем код
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
            recordsRepository.save(userRecordData.get(chatId));
            emailService.sendEmail(user.getEmail(), "Подтверждение регистрации: ",
                    String.valueOf(userRecordData.get(chatId).getAccessCode()));
        }
    }


    private void displayAllMasterName(Long chatId, String serviceName) { // вывод всех мастеров для данного сервиса
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
            log.error(errorText + e.getMessage());
        }
    }

    private void displayAllServiceName(Long chatId) { // вывод всех услуг
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
            log.error(errorText + e.getMessage());
        }
    }

    private void startCommandReceived(long chatId, String name) { // приветствие пользователю
        String greeting = "Здравствуйте, " + name + "!\n";
        String commands = "Список доступных команд:\n" + getListOfCommands();
        log.info("Hello to user " + name);
        sendMenuMessage(chatId, greeting + commands);
    }

    private String getListOfCommands() { // чтоб вывести список доступных команд
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

    private void sendMenuMessage(long chatId, String textToSend) { // вывод кнопок с записью и расписание
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

    private void sendStandartMessage(long chatId, String textToSend) { // формирование сообщения перед отправкой
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) { // отправка сообщения ботом
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(errorText + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) { // подготовить и отправить сообщение
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}") // в будущем полноценная рассылка рекламы
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
