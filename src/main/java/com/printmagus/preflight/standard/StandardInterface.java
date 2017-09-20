package com.printmagus.preflight.standard;

import com.printmagus.preflight.PreflightInterface;
import com.printmagus.preflight.rule.RuleInterface;

import java.util.List;

public interface StandardInterface extends PreflightInterface
{
    public List<RuleInterface> getRules();
}
