package com.becare.users.utils;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by neerajpaliwal on 06/07/16.
 */
public class FileUtils {
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static String readResourceToString(Context context, int id) throws IOException {

        InputStream inputStream = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            reader.close();
            inputStream.close();
        }

        return writer.toString();
    }
}
