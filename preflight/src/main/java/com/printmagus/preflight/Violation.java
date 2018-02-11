package com.printmagus.preflight;

import org.apache.pdfbox.pdmodel.PDPage;

import java.util.HashMap;

public class Violation
{
    private String rule;
    private String message;
    private Integer page;
    private HashMap<String, ?> context = null;

    public Violation(String rule, String message, Integer page)
    {
        this.rule = rule;
        this.message = message;
        this.page = page;
    }

    public Violation(String rule, String message, Integer page, HashMap<String, ?> context)
    {
        this.rule = rule;
        this.message = message;
        this.page = page;
        this.context = context;
    }

    @Override
    public String toString()
    {
        if (context == null || context.isEmpty()) {
            return String.format("[%s](%s) %s", rule, page == null ? "-" : page, message);
        }

        return String.format("[%s](%s) %s -> %s", rule, page == null ? "-" : page, message, context);
    }

    public String getMessage()
    {
        return message;
    }

    public String getRule()
    {
        return rule;
    }

    public Integer getPage()
    {
        return page;
    }

    public HashMap<String, ?> getContext()
    {
        return context;
    }
}
