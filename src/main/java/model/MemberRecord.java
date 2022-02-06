package model;

public class MemberRecord {
    private String id;
    private String name;
    private String identifier;
    private String telegramId;

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    public MemberRecord() {}

    public MemberRecord(String name, String identifier, String telegramId) {
        this.name = name;
        this.identifier = identifier;
        this.telegramId = telegramId;
    }
}
