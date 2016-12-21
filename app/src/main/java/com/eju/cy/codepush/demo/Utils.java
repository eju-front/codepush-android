package com.eju.cy.codepush.demo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by SidneyXu on 2016/11/14.
 */
/* package */ class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static String getAppVersion(final Context context) {
        String versionName = null;
        PackageInfo packInfo = getPackageInfo(context);
        if (packInfo != null) {
            versionName = packInfo.versionName;
        }
        return versionName;
    }

    private static PackageInfo getPackageInfo(final Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS
                            | PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to get the package info.", e);
        }
        return info;
    }

    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                delete(f);
            }
            file.delete();
        }
    }

    public static void pipe(InputStream inputStream, OutputStream outputStream) throws IOException {
        int len = -1;
        byte[] buffer = new byte[4098];
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.flush();
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len = -1;
        byte[] buffer = new byte[4098];
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    public static void writeBytes(byte[] bytes, OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }

    public static void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (null != closeable) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean copyAssetFolder(AssetManager assetManager,
                                          String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            Log.e(TAG, "Error occurs when copy asset foler", e);
            return false;
        }
    }

    public static boolean copyAsset(AssetManager assetManager,
                                    String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occurs when copy asset", e);
            return false;
        } finally {
            closeQuietly(out, in);
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static boolean copyChildren(String sourceRootPath, String targetRootPath) {
        File sourceRootDir = new File(sourceRootPath);
        File targetRootDir = new File(targetRootPath);
        if (sourceRootDir.isFile()) {
            return false;
        }
        File[] files = sourceRootDir.listFiles();
        for (File f : files) {
            if (!recursiveCopy(f, new File(targetRootDir, f.getName()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean recursiveCopy(File sourceFile, File targetFile) {
        if (sourceFile.isFile()) {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(targetFile);
                pipe(in, out);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                closeQuietly(out, in);
            }
            return true;
        }

        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        File[] files = sourceFile.listFiles();
        for (File f : files) {
            if (!recursiveCopy(f, new File(targetFile, f.getName()))) {
                return false;
            }
        }
        return true;
    }

    public static String download(String urlString, String filePath) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            File target = new File(filePath);
            target.createNewFile();
            output = new FileOutputStream(filePath);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            closeQuietly(output, input);
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    public static void decompressFile(File zipFile, File target) throws IOException {
        ZipInputStream zipInputStream = null;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        if (target.exists()) {
            Utils.delete(target);
        }
        target.mkdirs();
        try {
            fileInputStream = new FileInputStream(zipFile);
            zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(target, entry.getName());
                    dir.mkdirs();
                } else {
                    File file = new File(target, entry.getName());
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    Utils.pipe(zipInputStream, fileOutputStream);
                    fileOutputStream.close();
                    zipInputStream.closeEntry();
                }
            }
        } finally {
            Utils.closeQuietly(fileOutputStream, zipInputStream, fileInputStream);
        }
    }
}
