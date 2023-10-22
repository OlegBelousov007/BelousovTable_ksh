package com.digdes.school;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {
    private final DataStorage dataStorage = new DataStorage();
    private final StringManipulator stringManipulator = new StringManipulator();

    public JavaSchoolStarter() {
    }

    public List<Map<String, Object>> execute(String s) throws Exception {
        // get and split string
        stringManipulator.ParseString(s);
        // apply commands to data array
        var dataResult = dataStorage.ApplyAction(stringManipulator.getRequestedCommand(),
                stringManipulator.getListCondition(), stringManipulator.getListColumn());
        //return result rows
        return dataResult;
    }
}
