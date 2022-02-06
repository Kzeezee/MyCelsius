package model;

public class UserRecord {
    private String id;
    private String email;
    private Boolean hasOrg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getHasOrg() {
        return hasOrg;
    }

    public void setHasOrg(Boolean hasOrg) {
        this.hasOrg = hasOrg;
    }

    public UserRecord() {}

    public UserRecord(String id, String email, Boolean hasOrg) {
        this.id = id;
        this.email = email;
        this.hasOrg = hasOrg;
    }
}
