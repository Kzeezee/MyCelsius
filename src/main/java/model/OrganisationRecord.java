package model;

import lombok.Data;

@Data
public class OrganisationRecord {
    private String id; // ID is also the organisation code.
    private String name;
    private String[] admins;

    public OrganisationRecord(String name, String[] admins) {
        this.name = name;
        this.admins = admins;
    }
}
