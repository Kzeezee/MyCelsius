package util;

import io.github.cdimascio.dotenv.Dotenv;

public class Constants {
    // Validation for authentication
    public static final String EMAIL_REGEX_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    // Security constants
    public static final String INVALID_CREDENTIALS = "{{INVALID_CREDENTIALS}}";
    public static final String NOT_LOGGED_IN = "{{NO_USER_ID}}";

    // Styling constants
    public static final String SPACING_STYLE = "-fx-padding: 0 8 0 8;";
}
