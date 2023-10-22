package com.digdes.school;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.digdes.school.ConditionData.LogicOperators;
import com.digdes.school.ConditionData.Operators;

public class DataStorage {
    private final List<Map<String, Object>> dataTable;
    private final Map<String, String> tableStructure = new HashMap<>();

    public DataStorage() {
        dataTable = new ArrayList<>();
        //set table structure
        tableStructure.put("id", Long.class.getSimpleName());
        tableStructure.put("lastName", String.class.getSimpleName());
        tableStructure.put("age", Long.class.getSimpleName());
        tableStructure.put("cost", Double.class.getSimpleName());
        tableStructure.put("active", Boolean.class.getSimpleName());
    }

    public ArrayList<Map<String, Object>> ApplyAction(String command, List<ConditionData> conditions,
                                                      List<ConditionData> columns) throws Exception {
        var resultRows = new ArrayList<Map<String, Object>>();
        // just insert
        if (command.equals("insert")) {
            resultRows = InsertToStorage(columns);
            return resultRows;
        }
        // get row ids from conditions
        var listIds = MakeListIds(conditions);
        // execute actions
        resultRows = switch (command) {
            case "select" -> SelectFromStorage(listIds);
            case "update" -> UpdateToStorage(listIds, columns);
            case "delete" -> DeleteToStorage(listIds);
            default -> resultRows;
        };
        // return result
        return resultRows;
    }

    private ArrayList<Map<String, Object>> InsertToStorage(List<ConditionData> columns) {
        var resultRows = new ArrayList<Map<String, Object>>();
        Map<String, Object> newRow = new HashMap<>();
        // add columns
        for (ConditionData col : columns) {
            newRow.put(col.ColumnName, col.Value);
        }
        // store
        dataTable.add(newRow);
        // add to return result
        resultRows.add(newRow);
        // return result
        return resultRows;
    }

    private ArrayList<Map<String, Object>> UpdateToStorage(List<Integer> updateIds, List<ConditionData> columns) {
        var resultRows = new ArrayList<Map<String, Object>>();
        //get rows
        for (int index : updateIds) {
            var tempRow = dataTable.get(index);
            //update colum values
            for (ConditionData col : columns) {
                tempRow.put(col.ColumnName, col.Value);
            }
            //update row
            dataTable.set(index, tempRow);
            //add to result
            resultRows.add(tempRow);
        }
        // return result
        return resultRows;
    }

    private ArrayList<Map<String, Object>> DeleteToStorage(List<Integer> deleteIds) {
        var resultRows = new ArrayList<Map<String, Object>>();
        //get rows
        for (int index : deleteIds) {
            resultRows.add(dataTable.get(index));
            //remove
            dataTable.remove(index);
        }
        // return result
        return resultRows;
    }

    private ArrayList<Map<String, Object>> SelectFromStorage(List<Integer> selectIds) throws Exception {
        var resultRows = new ArrayList<Map<String, Object>>();
        //get rows
        for (int index : selectIds) {
            resultRows.add(dataTable.get(index));
        }
        // return result
        return resultRows;
    }

    private List<Integer> MakeListIds(List<ConditionData> conditions) throws Exception {
        var resultArray = new ArrayList<Integer>();
        int tableIdex = 0;
        // check
        if (conditions.isEmpty()) {
            for (Map<String, Object> ignored : dataTable) {
                resultArray.add(tableIdex);
                //next index row
                tableIdex++;
            }
            return resultArray;
        }

        // apply conditions
        for (Map<String, Object> dataRow : dataTable) {
            boolean resultCompare = false;
            for (ConditionData condition : conditions) {
                if (dataRow.containsKey(condition.ColumnName)) {
                    var colValue = dataRow.get(condition.ColumnName);
                    switch (tableStructure.get(condition.ColumnName)) {
                        case "Boolean":
                            if (condition.Operator != Operators.EQUAL && condition.Operator != Operators.NOTEQUAL)
                                throw new Exception("incompatible type");
                            break;
                        case "Long", "Double":
                            if (condition.Operator == Operators.LIKE || condition.Operator == Operators.ILIKE)
                                throw new Exception("incompatible type");
                            break;
                        case "String":
                            if (condition.Operator == Operators.GREAT || condition.Operator == Operators.GREATEQUAL
                                    || condition.Operator == Operators.LESS
                                    || condition.Operator == Operators.LESSEQUAL)
                                throw new Exception("incompatible type");
                            break;
                    }
                    var compRes = CompareValues(colValue, condition.Value, condition.Operator, tableStructure.get(condition.ColumnName));
                    if (condition.LogicOperator == LogicOperators.EMPTY) {
                        resultCompare = compRes;
                    } else {
                        if (condition.LogicOperator == LogicOperators.AND)
                            resultCompare = resultCompare && compRes;
                        if (condition.LogicOperator == LogicOperators.OR)
                            resultCompare = resultCompare || compRes;
                    }
                }
            } // end conditions
            if (resultCompare)
                resultArray.add(tableIdex);
            //next index row
            tableIdex++;
        } // end datatable
        //
        return resultArray;
    }

    private boolean CompareValues(Object left, Object right, Operators operator, String dataType) {
        switch (operator) {
            case EQUAL:
                return left.equals(right);
            case NOTEQUAL:
                return !left.equals(right);
            case LIKE:
                if (!dataType.equals("String"))
                    return false;
                return FindAndCompareSubStrings((String) left, (String) right);
            case ILIKE:
                if (!dataType.equals("String"))
                    return false;
                return FindAndCompareSubStrings(left.toString().toLowerCase(), right.toString().toLowerCase());
            case GREATEQUAL:
                if (dataType.equals("Long"))
                    return Long.parseLong(left.toString()) >= Long.parseLong(right.toString());
                if (dataType.equals("Double"))
                    return Double.parseDouble(left.toString()) >= Double.parseDouble(right.toString());

                return false;
            case GREAT:
                if (dataType.equals("Long"))
                    return Long.parseLong(left.toString()) > Long.parseLong(right.toString());
                if (dataType.equals("Double"))
                    return Double.parseDouble(left.toString()) > Double.parseDouble(right.toString());

                return false;
            case LESSEQUAL:
                if (dataType.equals("Long"))
                    return Long.parseLong(left.toString()) <= Long.parseLong(right.toString());
                if (dataType.equals("Double"))
                    return Double.parseDouble(left.toString()) <= Double.parseDouble(right.toString());

                return false;
            case LESS:
                if (dataType.equals("Long"))
                    return Long.parseLong(left.toString()) < Long.parseLong(right.toString());
                if (dataType.equals("Double"))
                    return Double.parseDouble(left.toString()) < Double.parseDouble(right.toString());

                return false;
        }
        return false;
    }

    private boolean FindAndCompareSubStrings(String left, String right) {
        if (!right.contains("%"))
            return left.equals(right);

        if (right.startsWith("%") && right.endsWith("%"))
            return left.contains(right.replace("%", ""));

        if (right.startsWith("%"))
            return left.endsWith(right.replace("%", ""));

        if (right.endsWith("%"))
            return left.startsWith(right.replace("%", ""));

        return false;
    }
}
