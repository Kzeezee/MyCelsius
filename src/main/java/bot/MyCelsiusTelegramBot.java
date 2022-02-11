package bot;

import com.google.cloud.Timestamp;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import io.github.cdimascio.dotenv.Dotenv;
import model.TemperatureRecord;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import util.Verification;

import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static util.Constants.*;
import static util.MyCelsiusUtils.isValidOrganisationCode;

public class MyCelsiusTelegramBot extends AbilityBot {

    private static final BareboneToggle toggle = new BareboneToggle();
    IFirebaseDB firebaseDB = new FirebaseDB();

    public static void setup() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Dotenv dotenv = Dotenv.load();
            MyCelsiusTelegramBot bot = new MyCelsiusTelegramBot(dotenv.get("TELEGRAM_BOT_TOKEN"), "MyCelsiusBot");
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public MyCelsiusTelegramBot(String token, String username) {
        super(token, username, toggle);
    }

    @Override
    public long creatorId() {
        return 5003829958l;
    }

    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("introduces the general usage of the bot")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hi! This is the MyCelsius bot aimed to simplify your temperature submission for your organisation. " +
                        "To start the temperature submission process, here are a list of commands:\n\n" +
                        "/submit - Temperature submission for members of an organisation.\n" +
                        "/submitguest - Temperature submission for guests of an organisation.\n\n" +
                        "If submitting as guests, please make sure your Telegram first name and last name at the point of submission matches the details that " +
                        "the organisation has of you."
                        , ctx.chatId()))
                .build();
    }

    // Submission of temperature for members of the organisation
    public ReplyFlow submit() {
        Reply submissionSuccess = Reply.of(upd -> silent.send("Success! Your temperature has been successfully recorded. " +
                "\n\nSubmitted for: " + getSubmissionFor() +
                "\nTime of submission: " +  getSubmissionAt(),
                getChatId(upd)), checkValidTemperatureForMembers());

        ReplyFlow temperatureSubmission = ReplyFlow.builder(db)
                .action(upd -> silent.send("Great! Now please submit your temperature.", getChatId(upd)))
                .onlyIf(validUserAndOrganisation())
                .next(submissionSuccess)
                .build();

        return ReplyFlow.builder(db)
                .action(upd -> silent.send("Please enter your organisation code for temperature submission first.", getChatId(upd)))
                .onlyIf(hasMessage("/submit"))
                .next(temperatureSubmission)
                .build();
    }

    // Submission of temperature for guests to an organisation
    public ReplyFlow submitGuest() {
        Reply submissionSuccess = Reply.of(upd -> silent.send("Success! Temperature submission as a guest has been successfully recorded. " +
                        "\n\nSubmitted for: " + getSubmissionFor() +
                        "\nTime of submission: " +  getSubmissionAt(),
                getChatId(upd)), checkValidTemperatureForGuests());

        ReplyFlow temperatureSubmission = ReplyFlow.builder(db)
                .action(upd -> silent.send("Great! Now please submit your temperature.", getChatId(upd)))
                .onlyIf(validOrganisation())
                .next(submissionSuccess)
                .build();

        return ReplyFlow.builder(db)
                .action(upd -> silent.send("Please enter the organisation code for temperature submission first.", getChatId(upd)))
                .onlyIf(hasMessage("/submitguest"))
                .next(temperatureSubmission)
                .build();
    }

    public Ability cancel() {
        return Ability.builder()
                .name("cancel")
                .info("Cancel current operation.")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    db.<Long, Integer>getMap("user_state_replies").remove(ctx.chatId());
                    silent.send("Operation cancelled.", ctx.chatId());
                })
                .build();
    }

    private long getChatId(Update update) {
        return update.getMessage().getChatId();
    }

    @NotNull
    private Predicate<Update> hasMessage(String msg) {
        return upd -> upd.getMessage().getText().equals(msg);
    }

    @NotNull
    private Predicate<Update> checkValidTemperatureForMembers() {
        return upd -> {
            String message = upd.getMessage().getText().trim();
            Map<String, String> orgMap = db.getMap(ORGANISATION_MAPPING);
            Long userId = upd.getMessage().getFrom().getId();
            String organisationCode = orgMap.get(userId.toString());
            if (!message.equals(organisationCode)) { // Prevent error message being sent from previous update of sending organisation code
                try {
                    Double temperature = Double.parseDouble(message);
                    if (temperature >= TEMPERATURE_LOWER_LIMIT && temperature <= TEMPERATURE_HIGHER_LIMIT) {
                        // Submit final temperature
                        silent.send("Submitting temperature...", getChatId(upd));
                        // Null for member name for now as we need to query official name from Firestore
                        TemperatureRecord temperatureRecord = new TemperatureRecord(organisationCode, null, userId.toString(), temperature, Timestamp.now(), true);
                        firebaseDB.submitTemperature(temperatureRecord);
                        System.out.println("Member submitted temperature: " + upd.getMessage().getFrom().getUserName() + " " + temperature);
                        orgMap.remove(userId);
                        return true;
                    } else {
                        silent.send("Please enter a valid temperature", getChatId(upd));
                        return false;
                    }
                } catch (NumberFormatException e) {
                    silent.send("Please enter a valid temperature", getChatId(upd));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    silent.send("Something went wrong when submitting temperature. Please try again.", getChatId(upd));
                    return false;
                }
            } else {
                return false;
            }
        };
    }

    @NotNull
    private Predicate<Update> checkValidTemperatureForGuests() {
        return upd -> {
            String message = upd.getMessage().getText().trim();
            Map<String, String> orgMap = db.getMap(ORGANISATION_MAPPING);
            Long userId = upd.getMessage().getFrom().getId();
            String organisationCode = orgMap.get(userId.toString());
            if (!message.equals(organisationCode)) { // Prevent error message being sent from previous update of sending organisation code
                try {
                    Double temperature = Double.parseDouble(message);
                    if (temperature >= TEMPERATURE_LOWER_LIMIT && temperature <= TEMPERATURE_HIGHER_LIMIT) {
                        // Submit final temperature
                        silent.send("Submitting temperature...", getChatId(upd));
                        // Will use Guest's Telegram first name and last name for name recording
                        User guest = upd.getMessage().getFrom();
                        String guestName = guest.getFirstName();
                        String lastName = guest.getLastName();
                        if (lastName != null) {
                            if (!guest.getLastName().equals("null")) {
                                if (!guest.getLastName().trim().isEmpty()) {
                                    guestName = guestName + " " + guest.getLastName();
                                }
                            }
                        }
                        TemperatureRecord temperatureRecord =
                                new TemperatureRecord(organisationCode, guestName, userId.toString(), temperature, Timestamp.now(), false);
                        firebaseDB.submitTemperature(temperatureRecord);
                        System.out.println("Guest submitted temperature: " + upd.getMessage().getFrom().getUserName() + " " + temperature);
                        orgMap.remove(userId);
                        return true;
                    } else {
                        silent.send("Please enter a valid temperature", getChatId(upd));
                        return false;
                    }
                } catch (NumberFormatException e) {
                    silent.send("Please enter a valid temperature", getChatId(upd));
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    silent.send("Something went wrong when submitting temperature. Please try again.", getChatId(upd));
                    return false;
                }
            } else {
                return false;
            }
        };
    }

    @NotNull
    private Predicate<Update> validUserAndOrganisation() {
        return upd -> {
            String message = upd.getMessage().getText().trim();
            // Need to check both user telegram id and organisation code valid
            // Then proceed with allowing next reply, else need to show error reason.
            if (message.length() == 6) { // Only proceed if length of text is valid for an org. code
                if (isValidOrganisationCode(message)) { // Additional check for text being valid organisation code regex to reduce unnecessary Firestore reads.
                    // Check firebase
                    silent.send("Validating organisation and sender...", getChatId(upd));
                    try {
                        Long userId = upd.getMessage().getFrom().getId();
                        Verification verification = firebaseDB.verifyValidUserAndOrganisation(userId, message);
                        if (verification.getSuccess()) {
                            // Store the organisation code in embedded database for the next step (temperature submission)
                            Map<String, String> orgMap = db.getMap(ORGANISATION_MAPPING);
                            orgMap.put(String.valueOf(userId), message);
                            return true;
                        } else {
                            silent.send(verification.getReason(), getChatId(upd)); // Will submit false afterwards to signify failing the check
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (message.length() > 0 && !message.equals("/submit")) {
                silent.send("Please enter a valid organisation code", getChatId(upd));
            }
            return false;
        };
    }

    @NotNull
    private Predicate<Update> validOrganisation() {
        return upd -> {
            String message = upd.getMessage().getText().trim();
            if (message.length() == 6) { // Only proceed if length of text is valid for an org. code
                if (isValidOrganisationCode(message)) { // Additional check for text being valid organisation code regex to reduce unnecessary Firestore reads.
                    // Check firebase
                    silent.send("Validating organisation...", getChatId(upd));
                    try {
                        Long userId = upd.getMessage().getFrom().getId();
                        Verification verification = firebaseDB.verifyValidOrganisation(message);
                        if (verification.getSuccess()) {
                            // Store the organisation code in embedded database for the next step (temperature submission)
                            Map<String, String> orgMap = db.getMap(ORGANISATION_MAPPING);
                            orgMap.put(String.valueOf(userId), message);
                            return true;
                        } else {
                            silent.send(verification.getReason(), getChatId(upd)); // Will submit false afterwards to signify failing the check
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (message.length() > 0 && !message.equals("/submitguest")) {
                silent.send("Please enter a valid organisation code", getChatId(upd));
            }
            return false;
        };
    }

    private String getSubmissionFor() {
        String now = new Date().toString();
        return (now.substring(0, 10) + " " + now.substring(now.length()-4));
    }

    private String getSubmissionAt() {
        String now = new Date().toString();
        return now.substring(11, now.length()-5);
    }
}
