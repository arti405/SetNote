package com.arti405.setnote.data.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.arti405.setnote.ui.editor.ExerciseBlock;
import com.arti405.setnote.ui.editor.SetRow;


public class PdfExporter {

    public static File exportSessionToPdf(
            Context ctx,
            String sessionTitle,
            String sessionDate,
            java.util.List<ExerciseBlock> blocks
    ) throws Exception {

        // A4 @ 72dpi-ish (Android PdfDocument nutzt "points")
        final int pageWidth = 595;
        final int pageHeight = 842;

        final int margin = 40;
        final int lineGap = 18;

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(12f);

        Paint boldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldPaint.setTextSize(12f);
        boldPaint.setFakeBoldText(true);

        PdfDocument doc = new PdfDocument();

        int pageNumber = 1;
        PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
        Canvas canvas = page.getCanvas();

        int y = margin;

        // Header
        canvas.drawText(safe(sessionTitle), margin, y, titlePaint);
        y += lineGap + 6;
        canvas.drawText(safe(sessionDate), margin, y, textPaint);
        y += lineGap + 10;

        // Column headers
        canvas.drawText("Exercise", margin, y, boldPaint);
        canvas.drawText("Set", 330, y, boldPaint);
        canvas.drawText("kg/lbs", 380, y, boldPaint);
        canvas.drawText("reps", 450, y, boldPaint);
        y += lineGap;

        // Divider line (optional)
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint);
        y += lineGap;

        for (ExerciseBlock b : blocks) {
            String exName = safe(b.exercise);
            if (exName.isEmpty()) exName = "Exercise";

            // Exercise title
            if (needsNewPage(y, pageHeight, margin)) {
                doc.finishPage(page);
                pageNumber++;
                page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
                canvas = page.getCanvas();
                y = margin;
            }

            canvas.drawText(exName, margin, y, boldPaint);
            y += lineGap;

            // Sets
            int setIndex = 1;
            for (SetRow s : b.sets) {
                if (needsNewPage(y, pageHeight, margin)) {
                    doc.finishPage(page);
                    pageNumber++;
                    page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
                    canvas = page.getCanvas();
                    y = margin;
                }

                canvas.drawText(" ", margin, y, textPaint);
                canvas.drawText("#" + setIndex, 330, y, textPaint);
                canvas.drawText(safe(s.weight), 380, y, textPaint);
                canvas.drawText(safe(s.reps), 450, y, textPaint);
                y += lineGap;

                // optional note line
                String note = safe(s.note);
                if (!note.isEmpty()) {
                    if (needsNewPage(y, pageHeight, margin)) {
                        doc.finishPage(page);
                        pageNumber++;
                        page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
                        canvas = page.getCanvas();
                        y = margin;
                    }
                    canvas.drawText("  note: " + trim(note, 80), margin + 10, y, textPaint);
                    y += lineGap;
                }

                setIndex++;
            }

            y += 6; // extra spacing after exercise
        }

        doc.finishPage(page);

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SetNote_" + ts + ".pdf";
        File outFile = new File(ctx.getCacheDir(), fileName);

        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }

    private static boolean needsNewPage(int y, int pageHeight, int margin) {
        return y > pageHeight - margin;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

