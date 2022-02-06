package model;

public class OrganisationRecord {
    private String id; // ID is also the organisation code.
    private String name;
    private String[] admins;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAdmins() {
        return admins;
    }

    public void setAdmins(String[] admins) {
        this.admins = admins;
    }

    public OrganisationRecord(String name, String[] admins) {
        this.name = name;
        this.admins = admins;
    }
}
