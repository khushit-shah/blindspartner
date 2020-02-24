package com.khushitshah.blindspartner.libs.Language;


import com.khushitshah.blindspartner.libs.Language.translators.WordTranslator;

public class LineSequencer {

    /**
     * @param srcBluePrint  Source Blue Print!!
     * @param destBluePrint The destination blueprint which contains placeholders for some component of src.
     * @param src           The source String
     * @param translator    The source object which can be asked for translation if translation of a word is not found!
     * @return String with destBlueprint processed with help of src and src blue print.
     */
    public static String sequence(String srcBluePrint, String destBluePrint, String src, WordTranslator translator) {

        srcBluePrint = srcBluePrint.replaceAll(",", " , ");
        srcBluePrint = srcBluePrint.replaceAll("\\.", " . ");
        srcBluePrint = srcBluePrint.replaceAll("[\\s]+", " ");


        src = src.replaceAll(",", " , ");
        src = src.replaceAll("\\.", " . ");
        src = src.replaceAll("[\\s]+", " ");


        System.out.println("srcBluePrint: " + srcBluePrint);
        System.out.println("destBluePrint: " + destBluePrint);
        System.out.println("src: " + src);

        String[] bluePrint = srcBluePrint.split("[\\s]+");
        String[] srcWords = src.split("[\\s]+");
        String[] variables = new String[50];

        int bluePrintIndex = 0;
        int srcWordIndex = 0;

        int variableIndex = 0;
        int curVariableStart = -1;
        int curVariableEnd = -1;

        while (srcWordIndex != srcWords.length) {

            if (bluePrintIndex >= bluePrint.length) {
                srcWordIndex = srcWords.length;
                break;
            }

            if (bluePrint[bluePrintIndex].equals(srcWords[srcWordIndex])) {
                if (curVariableStart != -1) {
                    // Ending of current variable.
                    curVariableEnd = srcWordIndex;
                    String currentVariable = concatStringArrayWithWordTranslation(srcWords, curVariableStart, curVariableEnd, translator);
                    variables[variableIndex++] = currentVariable;
                    curVariableStart = -1;
                }
                bluePrintIndex++;
                srcWordIndex++;
                continue;
            }

            if (bluePrint[bluePrintIndex].equals("$")) {
                if (curVariableStart == -1) {
                    curVariableStart = srcWordIndex;
                }
                bluePrintIndex++;
            }

            srcWordIndex++;
        }

        // If there is only a placeholder or the placeholder is the last word of blueprint.!
        if (curVariableStart != -1) {
            curVariableEnd = srcWordIndex;
            String currentVariable = concatStringArrayWithWordTranslation(srcWords, curVariableStart, curVariableEnd, translator);
            variables[variableIndex++] = currentVariable;
        }

        // now add everything to destblueprint!!.
        for (int i = 0; i < variableIndex; i++) {
            System.out.println("replacing $" + (i + 1) + " with " + variables[i]);
//            destBluePrint = destBluePrint.replaceAll("\\$" + (i + 1) + "[ \\^]", variables[i]);
            destBluePrint = destBluePrint.replaceAll("\\$" + (i + 1) + " ", variables[i]);

        }

        return destBluePrint;
    }


    private static String concatStringArrayWithWordTranslation(String[] arr, int start, int end, WordTranslator translator) {
        if (end > arr.length || start < 0) {
            return null;
        }


        StringBuilder variable = new StringBuilder();

        for (int i = start; i < end; i++) {
            if (translator != null)
                variable.append(translator.translate(arr[i])).append(" ");
            else
                variable.append(arr[i]).append(" ");
        }

        return variable.toString();
    }

}