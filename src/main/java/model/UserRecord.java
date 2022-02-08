package model;

public class UserRecord {
    private String id;
    private String email;
    private String organisationCode;
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

    public String getOrganisationCode() {
        return organisationCode;
    }

    public void setOrganisationCode(String organisationCode) {
        this.organisationCode = organisationCode;
    }

    public Boolean getHasOrg() {
        return hasOrg;
    }

    public void setHasOrg(Boolean hasOrg) {
        this.hasOrg = hasOrg;
    }

    public UserRecord() {}

    public UserRecord(String id, String email, String organisationCode, Boolean hasOrg) {
        this.id = id;
        this.email = email;
        this.organisationCode = organisationCode;
        this.hasOrg = hasOrg;
    }
}
