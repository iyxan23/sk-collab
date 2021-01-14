package com.iyxan23.sketch.collab;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Util {

    static boolean decryptError = false;

    public static String base64encode(String txt) {
        byte[] data = txt.getBytes();
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String base64decode(String base64) {
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data);
    }

    public static String decrypt_from_path(String path) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
            byte[] bArr = new byte[((int) randomAccessFile.length())];
            randomAccessFile.readFully(bArr);

            return decrypt(bArr);
        } catch (Exception e) {
            Log.e("Util", "Error while decrypting, at path: " + path);
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(byte[] data) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");;
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(2, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));

            return new String(instance.doFinal(data));
        } catch (Exception e) {
            Log.e("Util", "Error while decrypting");
            e.printStackTrace();
        }
        return "";
    }

    public static String sha512(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(input);
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(no.toString(16));

            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Copied from: https://www.journaldev.com/9400/android-external-storage-read-write-save-file
    @NonNull
    public static String readFile(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        FileInputStream fis = new FileInputStream(path);
        DataInputStream in = new DataInputStream(fis);
        BufferedReader br =
                new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            output.append(strLine);
        }
        in.close();
        return output.toString();
    }

    @NonNull
    public static byte[] readFile(final FileInputStream stream) {
        class Reader extends Thread {
            byte[] array = null;
        }

        Reader reader = new Reader() {
            public void run() {
                LinkedList<Pair<byte[], Integer>> chunks = new LinkedList<Pair<byte[], Integer>>();

                // read the file and build chunks
                int size = 0;
                int globalSize = 0;
                do {
                    try {
                        int chunkSize = 8192;
                        // read chunk
                        byte[] buffer = new byte[chunkSize];
                        size = stream.read(buffer, 0, chunkSize);
                        if (size > 0) {
                            globalSize += size;

                            // add chunk to list
                            chunks.add(new Pair<byte[], Integer>(buffer, size));
                        }
                    } catch (Exception e) {
                        // very bad
                    }
                } while (size > 0);

                try {
                    stream.close();
                } catch (Exception e) {
                    // very bad
                }

                array = new byte[globalSize];

                // append all chunks to one array
                int offset = 0;
                for (Pair<byte[], Integer> chunk : chunks) {
                    // flush chunk to array
                    System.arraycopy(chunk.first, 0, array, offset, chunk.second);
                    offset += chunk.second;
                }
            }
        };

        reader.start();
        try {
            reader.join();
        } catch (InterruptedException e) {
            Log.e("Util", "Failed on reading file from storage while the locking Thread", e);
            return null;
        }

        return reader.array;
    }

    public static byte[] joinByteArrays(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }
}
