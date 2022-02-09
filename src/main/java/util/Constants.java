package util;

public class Constants {
    // Firestore references
    public static final String USER_COLLECTION = "users";
    public static final String ORGANISATION_COLLECTION = "organisation";
    public static final String MEMBERS_COLLECTION = "members";
    public static final String TEMPERATURE_COLLECTION = "temperature";

    // Validation for authentication
    public static final String EMAIL_REGEX_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    // Application constants
    public static final Integer MAX_CHARTS_TO_BE_DISPLAYED = 5;

    // Telegram bot constants
    public static final Double TEMPERATURE_LOWER_LIMIT = 35.0;
    public static final Double TEMPERATURE_HIGHER_LIMIT = 45.0;
    public static final String ORGANISATION_MAPPING = "ORGANISATION_MAPPING";

    // Security constants
    public static final String INVALID_CREDENTIALS = "{{INVALID_CREDENTIALS}}";
    public static final String NOT_LOGGED_IN = "{{NO_USER_ID}}";
    public static final String NO_EMAIL = "{{NO_USER_EMAIL}}";
    public static final String NO_ORGANISATION = "{{NO_ORGANISATION}}";

    // Styling constants
    public static final String SPACING_STYLE = "-fx-padding: 0 8 0 8;";
    public static final String CHART_SELECTED_HIGHLIGHT = "-fx-background-color: #8affff;";
    public static final String CHART_UNSELECTED = "-fx-background-color: #f2f2f2;";
}
