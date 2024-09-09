package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class TypeFilter {

    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_YELLOW = "\u001B[33m";
    private final String ANSI_BOLD = "\u001B[1m";


    private String resultPath = "";
    private final List<String> filesPath;
    private String prefix = "";
    private final boolean add;
    private final String statistic;
    private List<String[]> vals;
    private final List<String> arguments;

    public TypeFilter(String[] args) {
        arguments = new ArrayList<>(List.of(args));
        add = arguments.contains("-a");
        arguments.remove("-a");

        if (arguments.contains("-help")) {
            showHelp();
            System.exit(0);
        }
        System.out.println(ANSI_BOLD + "Use \"-help\" to show usage information\n" + ANSI_RESET);
        if (arguments.contains("-o")) {
            resultPath = arguments.get(arguments.indexOf("-o") + 1);
            arguments.remove(arguments.indexOf("-o") + 1);
            arguments.remove("-o");

        }

        if (arguments.contains("-p")) {
            prefix = arguments.get(arguments.indexOf("-p") + 1);
            arguments.remove(arguments.indexOf("-p") + 1);
            arguments.remove("-p");

        }

        statistic = getStatistics();
        filesPath = new ArrayList<>(arguments);
    }

    void start() throws IOException, URISyntaxException {
        vals = scanFiles();
        writeResults();
        if (statistic.equals("s")) shorStatistic();
        else fullStatistic();
    }

    private void shorStatistic() {
        List<String[]> statistics = new ArrayList<>();
        int strCount = 0;
        int intCount = 0;
        int fltCount = 0;
        for (String[] line : vals) {
            switch (line[0]) {
                case "string" -> strCount++;
                case "int" -> intCount++;
                case "float" -> fltCount++;
            }
        }

        statistics.add(new String[]{"int", String.valueOf(intCount)});
        statistics.add(new String[]{"string", String.valueOf(strCount)});
        statistics.add(new String[]{"float", String.valueOf(fltCount)});

        String leftAlignFormat = "| %-6s | %-7s |%n";
        System.out.format("+--------+---------+%n");
        System.out.format("|  type  |  count  |%n");
        System.out.format("+--------+---------+%n");
        for (String[] line : statistics) {
            System.out.format(leftAlignFormat, line[0], line[1]);
        }
        System.out.format("+--------+---------+%n");
    }

    private void fullStatistic() {
        String[] line = getStatisticsForString();
        String leftAlignFormat = "| %-5s | %-7s | %-15s | %-14s |%n";
        if (!line[0].equals("0")) {
            System.out.format("+--------+---------+-----------------+----------------+%n");
            System.out.format("|  type  |  count  |  smallest size  |  biggest size  |%n");
            System.out.format("+--------+---------+-----------------+----------------+%n");
            System.out.format(leftAlignFormat, "string", line[0], line[1], line[2]);
            System.out.format("+--------+---------+-----------------+----------------+%n");
        }

        System.out.println();

        String[] line2 = getStatisticsForInteger();
        if (!line2[0].equals("0")) {
            leftAlignFormat = "| %-6s | %-7s | %-9s | %-13s |  %-11s  |  %-15s  |%n";
            System.out.format("+--------+---------+-----------+---------------+---------------+-------------------+%n");
            System.out.format("|  type  |  count  |    min    |      max      |      sum      |      average      |%n");
            System.out.format("+--------+---------+-----------+---------------+---------------+-------------------+%n");
            System.out.format(leftAlignFormat, "int", line2[0], line2[1], line2[2], line2[3], line2[4]);
            System.out.format("+--------+---------+-----------+---------------+---------------+-------------------+%n");
        }

        System.out.println();

        String[] line3 = getStatisticsForFloat();
        if (!line3[0].equals("0")) {
            leftAlignFormat = "| %-6s | %-7s | %-9s | %-25s |  %-23s  |  %-27s  |%n";
            System.out.format("+--------+---------+-----------+---------------------------+---------------------------+-------------------------------+%n");
            System.out.format("|  type  |  count  |    min    |            max            |            sum            |            average            |%n");
            System.out.format("+--------+---------+-----------+---------------------------+---------------------------+-------------------------------+%n");

            System.out.format(leftAlignFormat, "float", line3[0], line3[1], line3[2], line3[3], line3[4]);
            System.out.format("+--------+---------+-----------+---------------------------+---------------------------+-------------------------------+%n");
        }
    }


    private String[] getStatisticsForInteger() {

        int count = 0;
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        long sum = 0;
        float average;
        for (String[] line : vals) {
            if (line[0].equals("int")) {
                count++;
                min = Math.min(min, Long.parseLong(line[1]));
                max = Math.max(max, Long.parseLong(line[1]));
                sum += Long.parseLong(line[1]);
            }
        }
        average = (float) sum / count;
        return new String[]{count + "", min + "", max + "", sum + "", average + ""};
    }

    private String[] getStatisticsForFloat() {
        int count = 0;
        float min = Float.MAX_VALUE;
        float max = (float)Long.MIN_VALUE;
        float sum = 0;
        float average;
        for (String[] line : vals) {
            if (line[0].equals("float")) {
                count++;
                min = Math.min(min, Float.parseFloat(line[1]));
                max = Math.max(max, Float.parseFloat(line[1]));
                sum += Float.parseFloat(line[1]);
            }
        }
        average = sum / count;
        return new String[]{count + "", min + "", max + "", sum + "", average + ""};
    }

    private String[] getStatisticsForString() {
        int count = 0;
        long min = Long.MAX_VALUE;
        int max = 0;
        for (String[] line : vals) {
            if (line[0].equals("string")) {
                count++;
                min = Math.min(min, line[1].length());
                max = Math.max(max, line[1].length());
            }
        }
        return new String[]{count + "", min + "", max + ""};
    }


    private String getStatistics() {
        String stat = null;
        if (arguments.contains("-s") && arguments.contains("-f")) {
            System.out.println("You can choose only one option for statistics! Short will be used by default");
            arguments.remove("-s");
            arguments.remove("-f");
            stat = "s";
        } else if (!arguments.contains("-s") && !arguments.contains("-f")) {
            System.out.println("Please choose option for statistics! Short will be used by default.");
            stat = "s";
        } else {
            if (arguments.contains("-s")) {
                stat = "s";
                arguments.remove("-s");
            } else if (arguments.contains("-f")) {
                stat = "f";
                arguments.remove("-f");
            }
        }

        return stat;
    }

    private List<String[]> scanFiles() {
        List<String[]> vals = new ArrayList<>();
        for (String file : filesPath) {
            if (!(file.charAt(0) == '-')) {
                try {
                    File source = new File(file);
                    Scanner scanner = new Scanner(source);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        try {
                            Long.parseLong(line);
                            vals.add(new String[]{"int", line});
                        } catch (NumberFormatException e) {
                            try {
                                Float.parseFloat(line);
                                vals.add(new String[]{"float", line});
                            } catch (NumberFormatException e2) {
                                vals.add(new String[]{"string", line});
                            }
                        }

                    }
                    scanner.close();
                } catch (FileNotFoundException _) {
                    System.out.println(ANSI_RED + "File " + file + " not found!" + ANSI_RESET);
                    arguments.remove(file);
                    if (arguments.isEmpty()) {
                        System.out.println(ANSI_RED + "There is no files to scan!" + ANSI_RESET);
                        System.exit(1);
                    }
                }
            } else {
                System.out.println(ANSI_YELLOW + "Invalid argument " + "\"" + file + "\"!" + ANSI_RESET);
                arguments.remove(file);
                if (arguments.isEmpty()) {
                    System.out.println(ANSI_RED + "There are no files to scan! Exiting application..." + ANSI_RESET);
                    System.exit(1);
                }
            }
        }
        return vals;
    }

    private void writeResults() throws IOException, URISyntaxException {
        String getJarName = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
        String getWorkLocation = String.valueOf(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
                .replace("file:/", "")
                .replace(getJarName, "");

        if (!add) {
            if (new File(getWorkLocation + "/" + resultPath + "/" + prefix + "integers").exists()) {
                new File(getWorkLocation + "/" + resultPath + "/" + prefix + "integers").delete();
            }
            if (new File(getWorkLocation + "/" + resultPath + "/" + prefix + "strings").exists()) {
                new File(getWorkLocation + "/" + resultPath + "/" + prefix + "strings").delete();
            }
            if (new File(getWorkLocation + "/" + resultPath + "/" + prefix + "floats").exists()) {
                new File(getWorkLocation + "/" + resultPath + "/" + prefix + "floats").delete();
            }
        }

        String fullPath = getWorkLocation + "/" + resultPath;
        File resDir = new File(fullPath);
        File intResult = new File(fullPath + "/" + prefix + "integers.txt");
        FileWriter intWriter = null;

        File strResult = new File(fullPath + "/" + prefix + "strings.txt");
        FileWriter strWriter = null;

        File fltResult = new File(fullPath + "/" + prefix + "floats.txt");
        FileWriter fltWriter = null;

        resDir.mkdir();
        for (String[] result : vals) {
            if (result[0].equals("int"))
                writeValue(intResult, intWriter, result[1]);

            if (result[0].equals("string"))
                writeValue(strResult, strWriter, result[1]);


            if (result[0].equals("float"))
                writeValue(fltResult, fltWriter, result[1]);

        }
    }

    private void writeValue(File resultFile, FileWriter writer, String value) throws IOException {
        resultFile.createNewFile();
        if (resultFile.exists()) {
            if (writer == null) {
                writer = add ? new FileWriter(resultFile, true) : new FileWriter(resultFile);
            }
            writer.write(value + "\n");
            writer.flush();
        }
    }

    private void showHelp() {
        System.out.println(ANSI_BOLD + "Usage: java -jar <path to .jar file> [options] <files>" + ANSI_RESET);
        System.out.println("Use -o to set path for result files");
        System.out.println("Use -p to set prefix for result files");
        System.out.println("Use -a to not delete previous results");
        System.out.println("Use -s to show short statistics and -f to show full statistics. If no one of them are entered or" +
                " both of them are entered - short will be used by default");

    }
}

