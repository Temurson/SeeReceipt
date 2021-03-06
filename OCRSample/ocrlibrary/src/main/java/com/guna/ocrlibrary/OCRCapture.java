package com.guna.ocrlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by INTEL on 20-03-2018.
 */

public class OCRCapture {
    private static final String TAG = OCRCapture.class.getSimpleName();
    private Activity activity;
    private boolean useFlash;
    private boolean autoFocus;

    private OCRCapture(Activity activity) {
        this.activity = activity;
    }

    public static OCRCapture Builder(Activity activity) {
        return new OCRCapture(activity);
    }

    public boolean isUseFlash() {
        return useFlash;
    }

    public OCRCapture setUseFlash(boolean useFlash) {
        this.useFlash = useFlash;
        return this;
    }

    public boolean isAutoFocus() {
        return autoFocus;
    }

    public OCRCapture setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
        return this;
    }

    public String getTextFromImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = Util.decodeSampledBitmapFromResource(imagePath, 500, 500);//MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
            if (bitmap != null) {
                return getTextFromBitmap(bitmap);
            } else {
                return "Cannot read image from given path";
            }
        } else {
            return "Image path is not valid";
        }
    }

    public String getTextFromUri(Uri imageUri) {
        if (imageUri != null) {
            Bitmap bitmap = Util.decodeSampledBitmapFromResource(getPath(imageUri), 500, 500);
            if (bitmap == null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                Log.v(TAG, "Width : " + bitmap.getWidth() + ", Height : " + bitmap.getHeight());
                return getTextFromBitmap(bitmap);
            } else {
                return "Cannot read image from given path";
            }
        } else {
            return "Image path is not valid";
        }
    }

    public String getTextFromBitmap(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(activity).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        StringBuilder imageText = new StringBuilder();
        ArrayList<String>imageText2 = new ArrayList<String>();  ///////
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        TextBlock[] myTextBlocks = new TextBlock[textBlocks.size()];
        for (int i = 0; i < textBlocks.size(); i++) {
            myTextBlocks[i] = textBlocks.get(textBlocks.keyAt(i));
        }
        Arrays.sort(myTextBlocks, Util.TextBlockComparator);
        for (TextBlock textBlock : myTextBlocks) {
            if (imageText.toString().equals("")) {
                imageText.append(textBlock.getValue());
                imageText2.add(textBlock.getValue());            /////
            } else {
                imageText.append("\n");
                imageText.append(textBlock.getValue());
                imageText2.add(textBlock.getValue());           ////
            }
        }
        String sS = "foiahgiupag";
        for(String s : imageText2)  {
            sS += s;

        }
        Log.d("textTest", sS);

        Pattern addressPattern = Pattern.compile("(\\w+(\\s+\\w+)*),\\s\\w\\w\\s\\d{5}");
        Matcher addressMatcher = addressPattern.matcher(sS);
        if (addressMatcher.find()) {
            sS += "FOUND ADDRESS: " + addressMatcher.group(1);
        } else {
            sS += "CANNOT FIND ADDRESS";
        }

        return sS;
        //return imageText.toString();
    }

    public void buildWithRequestCode(int requestCode) {
        Intent intent = new Intent(activity, OcrCaptureActivity.class);
        intent.putExtra("UseFlash", useFlash);
        intent.putExtra("AutoFocus", autoFocus);
        activity.startActivityForResult(intent, requestCode);
    }

    private String getPath(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = activity.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
