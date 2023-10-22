package com.digdes.school;

public class ConditionData {
    public enum Operators {EQUAL, NOTEQUAL, LIKE, ILIKE, GREATEQUAL, LESSEQUAL, GREAT, LESS}

    public enum LogicOperators {EMPTY, AND, OR}

    public String ColumnName;
    public Operators Operator;
    public Object Value;
    public LogicOperators LogicOperator;

    public ConditionData() {
    }
}
