package com.printmagus.preflight;

import java.util.HashMap;

public class Violation {
    private String rule;
    private String message;
    private Integer page;
    private HashMap<String, ?> context = null;

    public Violation(String rule, String message, Integer page) {
        this.rule = rule;
        this.message = message;
        this.page = page;
    }

    public Violation(String rule, String message, Integer page, HashMap<String, ?> context) {
        this.rule = rule;
        this.message = message;
        this.page = page;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("[%s](%s) %s", rule, page == null ? "-" : page, message);
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, ?> getContext() {
        return context;
    }
}
