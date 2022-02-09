package model;

import lombok.Data;

@Data
public class MemberRecord {
    private String id;
    private String name;
    private String identifier;
    private String telegramId;

    public MemberRecord() {}

    public MemberRecord(String name, String identifier, String telegramId) {
        this.name = name;
        this.identifier = identifier;
        this.telegramId = telegramId;
    }
}
