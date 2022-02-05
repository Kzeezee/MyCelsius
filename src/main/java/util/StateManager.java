package util;

import static util.Constants.NOT_LOGGED_IN;

public class StateManager {
    private static String currentUserId = NOT_LOGGED_IN;

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(String currentUserId) {
        StateManager.currentUserId = currentUserId;
    }

    public static void logout() {
        StateManager.currentUserId = NOT_LOGGED_IN;
    }

    public static Boolean isLoggedIn() {
        if (!StateManager.currentUserId.equals(NOT_LOGGED_IN)) {
            return true;
        } else {
            return false;
        }
    }
}
