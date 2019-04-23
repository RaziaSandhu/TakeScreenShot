package com.takescreenshot;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Display mDisplay;
    String imagesUri;
    String path;
    Bitmap b;
    int totalHeight;
    int totalWidth;
    public static final int READ_PHONE = 110;
    String file_name = "Screenshot";
    File myPath;
    Button bt_take_screenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mDisplay = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PHONE);
            }
        }
        bt_take_screenshot = (Button) findViewById(R.id.bt_take_screenshot);
        bt_take_screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt_take_screenshot.setVisibility(View.GONE);
                takeScreenShot();
            }
        });
    }

    private void takeScreenShot() {

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ScreenShot/");

        if (!folder.exists()) {
            boolean success = folder.mkdir();
        }

        path = folder.getAbsolutePath();
        path = path + "/" + file_name + System.currentTimeMillis() + ".pdf";

        // List<Integer> hights = new ArrayList<>();
        View u = findViewById(R.id.scroll);

        NestedScrollView z = (NestedScrollView) findViewById(R.id.scroll);
        totalHeight = z.getChildAt(0).getHeight();
        totalWidth = z.getChildAt(0).getWidth();

        Log.e("totalHeight--", "" + totalHeight);
        Log.e("totalWidth--", "" + totalWidth);

        //Save bitmap
        String extr = Environment.getExternalStorageDirectory() + "/ScreenShot/";
        File file = new File(extr);
        if (!file.exists())
            file.mkdir();
        String fileName = file_name + ".jpg";
        myPath = new File(extr, fileName);
        imagesUri = myPath.getPath();
        FileOutputStream fos = null;
        b = getBitmapFromView(u, totalHeight, totalWidth);

        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        createPdf();

    }

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {

        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    private void createPdf() {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(b.getWidth(), b.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        Bitmap bitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
        File filePath = new File(path);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();


        if (myPath.exists())
            myPath.delete();

        openPdf(path);
    }

    public void openPdf(String path) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app to read PDF File", Toast.LENGTH_LONG).show();
        }
    }
}
