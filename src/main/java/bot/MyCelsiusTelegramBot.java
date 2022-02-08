package bot;

import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import util.Verification;

import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static util.MyCelsiusUtils.isValidOrganisationCode;

public class MyCelsiusTelegramBot extends AbilityBot {

    private static final BareboneToggle toggle = new BareboneToggle();
    IFirebaseDB firebaseDB = new FirebaseDB();

    public static void setup() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Dotenv dotenv = Dotenv.load();
            MyCelsiusTelegramBot bot = new MyCelsiusTelegramBot(dotenv.get("TELEGRAM_BOT_TOKEN"), "MyCelsiusBot");
//            bot.executeAsync(BotApiMethod<Verification>)
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
                .action(ctx -> silent.send("Hi! This is the MyCelsius bot aimed to simplify your temperature submission for your organisation." +
                        "\n\nTo start the temperature submission process, please type /submit if you are a member of a organisation or /submitguest " +
                        "if you are a guest/visitor.", ctx.chatId()))
                .build();
    }

    public ReplyFlow submit() {
        // TODO: Inline keyboard, invalid staff/organisation and guest submission process.
        Reply submissionSuccess = Reply.of(upd -> silent.send("Success! Your temperature has been successfully recorded. " +
                "\n\nSubmitted for: " + getSubmissionFor() +
                "\nTime of submission: " +  getSubmissionAt(),
                getChatId(upd)), checkValidTemperature());

        Reply invalidOrganisation = Reply.of(upd -> silent.send("You have entered an invalid organisation code! Please retry the submission again. ",
                getChatId(upd)), invalidOrganisationCode());

        Reply invalidMember = Reply.of(upd -> silent.send("Your user ID is not recognised as a organisation member. " +
                        "Please use /submitguest if you are a guest or contact your admins for more information.",
                getChatId(upd)), validMember());

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

    public ReplyFlow submitGuest() {
        // TODO: Inline keyboard, invalid staff/organisation and guest submission process.
        Reply submissionSuccess = Reply.of(upd -> silent.send("Success! Your temperature has been successfully recorded for " +
                new Date().toString() + ".", getChatId(upd)), checkValidTemperature());

        Reply invalidOrganisation = Reply.of(upd -> silent.send("You have entered an invalid organisation code! Please retry the submission again. ",
                getChatId(upd)), checkValidTemperature());

        Reply invalidMember = Reply.of(upd -> silent.send("Your user ID is not recognised as a organisation member. Please submit the form again.",
                getChatId(upd)), validMember());

        ReplyFlow temperatureSubmission = ReplyFlow.builder(db)
                .action(upd -> silent.send("Great! Now please submit your temperature.", getChatId(upd)))
                .onlyIf(validUserAndOrganisation())
                .next(submissionSuccess)
                .build();

        return ReplyFlow.builder(db)
                .action(upd -> silent.send("Please enter your organisation code for temperature submission first.", getChatId(upd)))
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
    private Predicate<Update> checkValidTemperature() {
        return upd -> upd.getMessage().getText().equals("35");
    }

    @NotNull
    private Predicate<Update> validUserAndOrganisation() {
        return upd -> {
            String message = upd.getMessage().getText().trim();
            // Need to check both user telegram id and organisation code valid
            // Then proceed with allowing next reply, else need to throw message and stop ability
            if (message.length() == 6) { // Only proceed if length of text is valid for an org. code
                if (isValidOrganisationCode(message)) { // Additional check for text being valid organisation code regex to reduce unnecessary Firestore reads.
                    // Check firebase
                    try {
                        Long personId = upd.getMessage().getFrom().getId();
                        Verification verification = firebaseDB.verifyValidUserAndOrganisation(personId, message);
                        if (verification.getSuccess()) {
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
    private Predicate<Update> invalidOrganisationCode() {
        return upd -> !upd.getMessage().getText().equals("deez");
    }

    @NotNull
    private Predicate<Update> validMember() {
        return upd -> upd.getMessage().getText().equals("checkstaff");
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
