package util;

import model.UserRecord;

import static util.Constants.*;

/*
    Acts as a session manager for the user. Handles holding current logged-in user and other user information.
 */
public class StateManager {
    private static String currentOrg = NO_ORGANISATION;
    private static UserRecord currentUser = new UserRecord(NOT_LOGGED_IN, NO_EMAIL, false);

    public static void logout() {
        currentUser = new UserRecord(NOT_LOGGED_IN, NO_EMAIL, false);
        currentOrg = NO_ORGANISATION;
    }

    public static Boolean isLoggedIn() {
        if (StateManager.currentUser == null) {
            return false;
        } else {
            if (!currentUser.getId().equals(NOT_LOGGED_IN)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static String getCurrentUserId() {
        return currentUser.getId();
    }

    public static void setCurrentUser(UserRecord userRecord) {
        StateManager.currentUser = userRecord;
    }

    public static Boolean getUserHasOrg() {
        return StateManager.currentUser.getHasOrg();
    }

    public static String getCurrentOrg() {
        return currentOrg;
    }

    public static void setCurrentOrg(String currentOrg) {
        StateManager.currentOrg = currentOrg;
    }
}
