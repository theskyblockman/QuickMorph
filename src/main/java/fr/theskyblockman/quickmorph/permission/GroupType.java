package fr.theskyblockman.quickmorph.permission;

public enum GroupType {
    DEFAULT("default"), OP("op"), CUSTOM("custom");

    public final String displayName;

    GroupType(String displayName) {
        this.displayName = displayName;
    }
}
