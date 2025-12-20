package org.nikolic.programm.entities;

public enum CodePurpose {
    RESET("reset"),
    VERIFY("verify");

    private final String value;

    CodePurpose(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}