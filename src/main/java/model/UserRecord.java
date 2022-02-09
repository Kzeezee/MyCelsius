package model;

import lombok.Data;

@Data
public class UserRecord {
    private String id;
    private String email;
    private String organisationCode;
    private Boolean hasOrg;

    public UserRecord() {}

    public UserRecord(String id, String email, String organisationCode, Boolean hasOrg) {
        this.id = id;
        this.email = email;
        this.organisationCode = organisationCode;
        this.hasOrg = hasOrg;
    }
}
