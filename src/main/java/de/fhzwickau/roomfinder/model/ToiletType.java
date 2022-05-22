package de.fhzwickau.roomfinder.model;

public enum ToiletType {
    MALE(1), FEMALE(2), ACCESSIBLE(3);

    private byte id;

    private ToiletType(int id) {
        this((byte) id);
    }

    private ToiletType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
