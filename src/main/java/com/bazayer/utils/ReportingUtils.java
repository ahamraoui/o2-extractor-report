package com.bazayer.utils;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportingUtils {

    public static String getDateFormat() {
        Date date = new Date();
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String getInputFileName(String fileName, String output) {
        File temp = new File(output);
        if(!temp.exists()) {
            temp.mkdirs();
        }

        String filename = String.format(output.concat(fileName), ReportingUtils.getDateFormat());
        File file = new File(filename);
        if(file.exists()){
            file.delete();
        }
        System.out.println("file created in: " + filename);
        return filename;
    }

    public static void writeCsv(List<String[]> stringArray, String fileName, String folder) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(ReportingUtils.getInputFileName("/" + fileName + "_%s.csv", folder)), ',', '"', '"', "\n");
        for (String[] array : stringArray) {
            writer.writeNext(array);
        }
        writer.close();
    }
}
