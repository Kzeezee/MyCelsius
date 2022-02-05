package bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class MyCelsiusTelegramBot extends AbilityBot {

    private static final BareboneToggle toggle = new BareboneToggle();

    public static void setup() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            Dotenv dotenv = Dotenv.load();
            telegramBotsApi.registerBot(new MyCelsiusTelegramBot(dotenv.get("TELEGRAM_BOT_TOKEN"), "MyCelsiusBot"));
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
                        "\n\nTo start, please type /submit to begin the temperature submission process.", ctx.chatId()))
                .build();
    }

    public ReplyFlow submit() {
        // TODO: Inline keyboard, invalid staff/organisation and guest submission process.
        Reply submissionSuccess = Reply.of(upd -> silent.send("Success! Your temperature has been successfully recorded for " +
                new Date().getDate() + ".\nSubmission date: " +  new Date().toString(),
                getChatId(upd)), checkValidTemperature());

        Reply invalidOrganisation = Reply.of(upd -> silent.send("You have entered an invalid organisation code! Please retry the submission again. ",
                getChatId(upd)), invalidOrganisationCode());

        Reply invalidMember = Reply.of(upd -> silent.send("Your user ID is not recognised as a organisation member. " +
                        "Please use /submitguest if you are a guest or contact your admins for more information.",
                getChatId(upd)), validMember());

        ReplyFlow temperatureSubmission = ReplyFlow.builder(db)
                .action(upd -> silent.send("Great! Now please submit your temperature.", getChatId(upd)))
                .onlyIf(validOrganisationCode())
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
                .onlyIf(validOrganisationCode())
                .next(submissionSuccess)
                .build();

        return ReplyFlow.builder(db)
                .action(upd -> silent.send("Please enter your organisation code for temperature submission first.", getChatId(upd)))
                .onlyIf(hasMessage("/submitguest"))
                .next(temperatureSubmission)
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
    private Predicate<Update> validOrganisationCode() {
        return upd -> upd.getMessage().getText().equals("deez");
    }

    @NotNull
    private Predicate<Update> invalidOrganisationCode() {
        return upd -> !upd.getMessage().getText().equals("deez");
    }

    @NotNull
    private Predicate<Update> validMember() {
        return upd -> upd.getMessage().getText().equals("checkstaff");
    }
}
