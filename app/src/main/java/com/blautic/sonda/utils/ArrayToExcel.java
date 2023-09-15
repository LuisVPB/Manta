package com.blautic.sonda.utils;


import static org.apache.poi.ss.usermodel.ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
    private List<String> sql;
    private List<String> sheetName;
    private Workbook workbook;


    public static class Builder {
        private String filePath;
        private String fileName;
        private List<String> tables;
        private List<String> sheetName  = new ArrayList<>();
        private Context context;
        private int countSheet;

        public Builder(Context context) {
            this.context = context;
        }

        public ArrayToExcel build() {
            if (TextUtils.isEmpty(filePath)) {
                this.filePath = context.getExternalFilesDir(null).getPath();
            }
            if (TextUtils.isEmpty(fileName)) {
                this.fileName = "capturas_sonda.xls";
            }
            return new ArrayToExcel(tables, fileName, filePath, sheetName);
        }

        /**
         * @param fileName
         * @return Builder
         * @deprecated Use {@link #setOutputFileName(String fileName)} instead.
         */
        @Deprecated
        public  Builder setFileName(String fileName) {
            return setOutputFileName(fileName);
        }

        public  Builder setOutputFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }


        public  Builder setTables(String... tables) {
            this.tables = Arrays.asList(tables);
            return this;
        }

        /**
         * @param path
         * @return Builder
         * @deprecated Use {@link #setOutputPath(String path)} instead.
         */
        @Deprecated
        public  Builder setPath(String path) {
            return setOutputPath(path);
        }

        public  Builder setOutputPath(String path) {
            this.filePath = path;
            return this;
        }

        public String start() throws Exception {
            final  ArrayToExcel arrayToExcel = build();
            return arrayToExcel.start();
        }

        public void start( ExportListener listener) {
            final  ArrayToExcel arrayToExcel = build();
            arrayToExcel.start2(listener);
        }
    }

    /**
     * import Tables task
     *
     * @return output file path
     */
    public String start() throws Exception {
        String[][] arrayPrueba = {{"data_time", "pres1", "pres2", "pres3", "pres4", "pres5", "pres6"},
                {"15-09-23 / 13:15", "20%", "30%", "0%", "10%", "10%", "10%"}};
        try {
            if (tables == null || tables.size() == 0) {
                Log.d("info", "tabla vacía, contenido: "+tables.toString());
            }
            return exportTables(arrayPrueba, fileName);

        } catch (Exception e) {
            Log.d("info", "fallo al iniciar la exportación: "+e.getLocalizedMessage());
            throw e;
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
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (tables == null || tables.size() == 0) {
                        Log.d("info", "tabla vacía, contenido: "+tables.toString());
                    }
                    String[][] arrayPrueba = {{"data_time", "pres1", "pres2", "pres3", "pres4", "pres5", "pres6"},
                            {"15-09-23 / 13:15", "20%", "30%", "0%", "10%", "10%", "10%"}};
                    final String finalFilePath = exportTables(arrayPrueba, fileName);
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
            }
        }).start();
    }

    private ArrayToExcel(List<String> tables, String fileName,
                          String filePath, List<String> sheetName) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.sheetName = sheetName;
        this.tables = tables;
    }

    /**
     * core code, export array to a excel file
     *
     * @param fileName target file name
     * @return target file path
     */
    private String exportTables(String[][] data, final String fileName) {
        // Crear un nuevo libro de Excel
        if (fileName.toLowerCase().endsWith(".xls")) {
            workbook = new XSSFWorkbook();
        } else {
            throw new IllegalArgumentException("File name is null or unsupported file format!");
        }

        // Crear una nueva hoja de Excel
        LocalDateTime locaDate = LocalDateTime.now();
        int hours  = locaDate.getHour();
        int minutes = locaDate.getMinute();
        Sheet sheet = workbook.createSheet("presiones_"+hours+"_"+minutes);

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
