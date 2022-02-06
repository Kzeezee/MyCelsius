package util;

public class Verification {
    private Boolean success;
    private String reason;

    public Verification(Boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
}
