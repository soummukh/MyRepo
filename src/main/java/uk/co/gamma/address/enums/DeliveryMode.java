package uk.co.gamma.address.enums;

public enum DeliveryMode {
    FOOT("FOOT"), DRIVE("DRIVE");

    String mode;

    DeliveryMode(String mode){
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
}
