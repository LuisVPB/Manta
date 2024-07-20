package com.blautic.sonda.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayToExcel {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private String fileName;
    private String filePath;
    private List<String> tables;
    private Workbook workbook;
    String[][] datos;
    String userCode;

    //private String[][] expArray;


    public static class Builder {
        private String filePath;
        private String fileName;
        private List<String> tables;
        private List<String> sheetName  = new ArrayList<>();
        private Context context;
        private String[][] datos;
        private String userCode;

        public Builder(Context context, String[][] datos, String userCode) {
            this.context = context;
            this.datos = datos;
            this.userCode = userCode;
        }

        public ArrayToExcel build() {
            if (TextUtils.isEmpty(filePath)) {
                this.filePath = context.getExternalFilesDir(null).getPath();
            }
            if (TextUtils.isEmpty(fileName)) {
                this.fileName = "capturas_sonda.xls";
            }

            return new ArrayToExcel(tables, fileName, filePath, datos, userCode);
        }

        public  Builder setOutputFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public  Builder setTables(String tables) {
            this.tables = Arrays.asList(tables);
            return this;
        }

        public  Builder setOutputPath(String path) {
            this.filePath = path;
            return this;
        }

        public void start( ExportListener listener) {
            final  ArrayToExcel arrayToExcel = build();
            arrayToExcel.start2(listener);
        }
    }

    /**
     * importTables task with a listener
     *
     * @param listener callback
     */
    public void start2(final  ExportListener listener) {
        if (listener != null) {
            listener.onStart();
        }
        new Thread(() -> {
            try {
                if (tables == null || tables.size() == 0) {
                    Log.d("info", "tabla vacía, contenido: ");
                }

                final String finalFilePath = exportTables(datos, fileName, userCode);

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(finalFilePath);
                        }
                    });
                }
            } catch (final Exception e) {
                if (listener != null)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e);
                        }
                    });
            }
        }).start();
    }

    private ArrayToExcel(
            List<String> tables,
            String fileName,
            String filePath,
            String[][] datos,
            String userCode
    ) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.tables = tables;
        this.datos = datos;
        this.userCode = userCode;
    }

    /**
     * core code, export array to a excel file
     *
     * @param fileName target file name
     * @return target file path
     */
    private String exportTables(String[][] data, final String fileName, String userCode) {
        // Crear un nuevo libro de Excel
        if (fileName.toLowerCase().endsWith(".xls")) {
            workbook = new XSSFWorkbook();
        } else {
            throw new IllegalArgumentException("File name is null or unsupported file format!");
        }

        // Crear una nueva hoja de Excel
        LocalDateTime locaDate = LocalDateTime.now();
        int day  = locaDate.getDayOfMonth();
        int month  = locaDate.getMonthValue();
        int year  = locaDate.getYear();
        int hours  = locaDate.getHour();
        int minutes = locaDate.getMinute();
        Sheet sheet = workbook.createSheet(userCode+"_"+day+"-"+month+"-"+year+"_"+hours+"_"+minutes); //título de la hoja
        Log.d("info", userCode+"_"+day+"/"+month+"/"+year+"_"+hours+"_"+minutes);

        // Iterar sobre el array de datos y escribirlo en el archivo Excel
        for (int rowIndex = 0; rowIndex < data.length; rowIndex++) {
            Row row = sheet.createRow(rowIndex);
            for (int colIndex = 0; colIndex < data[rowIndex].length; colIndex++) {
                Cell cell = row.createCell(colIndex);
                cell.setCellValue(data[rowIndex][colIndex]);
            }
        }

        // Guardar el archivo Excel en el sistema de archivos
        File file = new File(filePath, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
            if (outputStream != null) {
                outputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getPath();
    }


    /**
     * Callbacks for export events.
     */
    public interface ExportListener {
        void onStart();

        void onCompleted(String filePath);

        void onError(Exception e);
    }



}
