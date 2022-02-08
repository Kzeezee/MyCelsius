package model;

public class GuestTemperatureRecord extends TemperatureRecord {
    private Boolean isGuest;

    public GuestTemperatureRecord() {
        this.isGuest = true;
    }

    public Boolean getIsGuest() {
        return isGuest;
    }
}
