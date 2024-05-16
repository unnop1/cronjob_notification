package com.nt.cronjob_notification.util;

public class Condition {
    public static final boolean doNumberOperation(String operator,Integer dataNumber, Integer orConfNumber){
        // int dataNumber = Integer.parseInt(dataValue);
        // int orConfNumber = Integer.parseInt(orConfValue);
        // System.out.println(operator+" , dataNumber: "+dataNumber+ " , orConfNumber:"+ orConfNumber + " ==> "+(!dataNumber.equals(orConfNumber)));
        switch (operator) {
            case "!=":
                return !dataNumber.equals(orConfNumber);
            case "=":
                return dataNumber.equals(orConfNumber);
            case "<":
                return dataNumber < orConfNumber;
            case ">":
                return dataNumber > orConfNumber;    
            case "<=":
                return dataNumber <= orConfNumber; 
            case ">=":
                return dataNumber >= orConfNumber; 
            default:
                return false;
        }
    }
}
