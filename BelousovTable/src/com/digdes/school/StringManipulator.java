package com.digdes.school;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringManipulator {
    private final List<ConditionData> listCondition;
    private final List<ConditionData> listColumns;
    private String requestedCommand;
    private final List<String> allowedCommands = new ArrayList<>();

    public StringManipulator() {
        listCondition = new ArrayList<>();
        listColumns = new ArrayList<>();
        allowedCommands.add("INSERT".toLowerCase());
        allowedCommands.add("UPDATE".toLowerCase());
        allowedCommands.add("DELETE".toLowerCase());
        allowedCommands.add("SELECT".toLowerCase());
    }

    private void Clear() {
        requestedCommand = "";
        listCondition.clear();
        listColumns.clear();
    }

    public List<ConditionData> getListCondition() {
        return listCondition;
    }

    public List<ConditionData> getListColumn() {
        return listColumns;
    }

    public String getRequestedCommand() {
        return requestedCommand;
    }

    public void ParseString(String s) throws Exception {
        // clear
        Clear();
        String workStr = s.trim().toLowerCase();
        List<String> stringList = Arrays.asList(workStr.split(" "));
        // 1. get command
        requestedCommand = stringList.get(0);
        if (!allowedCommands.contains(requestedCommand)) {
            requestedCommand = "";
            throw new Exception("no command!");
        }
        workStr = workStr.replace(requestedCommand, "").trim();
        if (workStr.isEmpty())
            return;
        // 2. get where, if not insert command
        workStr = s.trim().toLowerCase();
        var indexWhere = workStr.indexOf("where");
        if (!requestedCommand.equals("insert")) {
            if (indexWhere >= 0) {
                // var whereStr = workStr.substring(indexWhere + "where".length()).trim();
                var whereIndex = indexWhere + "where".length();
                if (whereIndex < workStr.length()) {
                    do {
                        var indexLogicOperatorAnd = workStr.indexOf("and", whereIndex);
                        var indexLogicOperatorOr = workStr.indexOf("or", whereIndex);

                        var lastWhereIndex = workStr.length();
                        if (indexLogicOperatorAnd > 0)
                            lastWhereIndex = indexLogicOperatorAnd - 1;
                        if (indexLogicOperatorOr > 0)
                            lastWhereIndex = indexLogicOperatorOr - 1;

                        var tempString = s.substring(whereIndex, lastWhereIndex).trim();
                        var tempCondition = SplitStringToCondition(tempString);
                        whereIndex = lastWhereIndex;
                        if (tempCondition == null)
                            throw new Exception("error parse WHERE statement");

                        if (indexLogicOperatorAnd != -1) {
                            tempCondition.LogicOperator = ConditionData.LogicOperators.AND;
                            whereIndex += "and".length() + 1 + 1;
                        }
                        if (indexLogicOperatorOr != -1) {
                            tempCondition.LogicOperator = ConditionData.LogicOperators.OR;
                            whereIndex += "or".length() + 1 + 1;
                        }
                        // store
                        listCondition.add(tempCondition);
                    } while (whereIndex < workStr.length());

                }
            }
        }
        // if not where, then set full length
        if (indexWhere < 0)
            indexWhere = workStr.length();
        // 3. get columns, for insert,update (after values)
        var indexValues = workStr.indexOf("values");
        if (indexValues >= 0) {
            var valuesStr = s.substring(indexValues + "values".length(), indexWhere).trim();
            if (!valuesStr.isEmpty()) {
                var listBlocks = valuesStr.split(",");
                for (String wBlock : listBlocks) {
                    listColumns.add(SplitStringToCondition(wBlock.trim()));
                }
            }
        }
    }

    private ConditionData SplitStringToCondition(String str) throws Exception {
        if (str.isEmpty())
            return null;

        if (str.contains(">=")) {
            var temp = MakeNewConditionData(str, ">=");
            temp.Operator = ConditionData.Operators.GREATEQUAL;
            return temp;
        }
        if (str.contains("<=")) {
            var temp = MakeNewConditionData(str, "<=");
            temp.Operator = ConditionData.Operators.LESSEQUAL;
            return temp;
        }
        if (str.contains(">")) {
            var temp = MakeNewConditionData(str, ">");
            temp.Operator = ConditionData.Operators.GREAT;
            return temp;
        }
        if (str.contains("<")) {
            var temp = MakeNewConditionData(str, "<");
            temp.Operator = ConditionData.Operators.LESS;
            return temp;
        }
        if (str.contains("!=")) {
            var temp = MakeNewConditionData(str, "!=");
            temp.Operator = ConditionData.Operators.NOTEQUAL;
            return temp;
        }
        if (str.contains("=")) {
            var temp = MakeNewConditionData(str, "=");
            temp.Operator = ConditionData.Operators.EQUAL;
            return temp;
        }
        if (str.contains("like")) {
            var temp = MakeNewConditionData(str, "like");
            temp.Operator = ConditionData.Operators.LIKE;
            return temp;
        }
        if (str.contains("ilike")) {
            var temp = MakeNewConditionData(str, "ilike");
            temp.Operator = ConditionData.Operators.ILIKE;
            return temp;
        }

        throw new Exception("error action in WHERE");
    }

    private ConditionData MakeNewConditionData(String str, String delimiter) {
        var result = new ConditionData();

        var temp = str.split(delimiter);
        result.ColumnName = temp[0].replace("'", "").trim();
        result.Operator = ConditionData.Operators.EQUAL;
        result.Value = temp[1].replace("'", "").trim();
        result.LogicOperator = ConditionData.LogicOperators.EMPTY;

        return result;
    }
}
