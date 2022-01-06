package com.company;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.Inflater;

public class Main {

    private static String EXPORT_DIR = "output";

    private static void saveClasses(File jar, ResultSet classes)  {
        try(JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))){
            while(classes.next()){
                String name = classes.getString(2);
                String path = classes.getString(3);
                JarEntry e = new JarEntry(path+"/"+name);
                out.putNextEntry(e);
                byte[] streamBytes = null;
                Blob streamBlob = classes.getBlob(4);
                long streamLength = streamBlob.length();
                if (streamLength <= 2147483647L) {
                    streamBytes = streamBlob.getBytes(1L, (int)streamLength);
                }
                out.write(inflate(streamBytes));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readJars(String url, String username, String password, String jarListSQL, String classesSql) {
        try (Connection conn =
                     DriverManager.getConnection(url, username, password)) {
            PreparedStatement stmt = conn.prepareStatement(jarListSQL);
            ResultSet rs = stmt.executeQuery();
            long startTime = System.nanoTime();
            System.out.println("Start!");
            while(rs.next()) {
                String currentJar = rs.getString(1);
                System.out.printf("\nFetch classes for jar: %s", currentJar);
                stmt = conn.prepareStatement(classesSql);
                stmt.setString(1, currentJar);
                ResultSet classes = stmt.executeQuery();
                File jar = new File(EXPORT_DIR, currentJar);
                jar.getParentFile().mkdirs();
                saveClasses(jar, classes);
            }
            System.out.printf("\nEnd. Time elapsed: %d", System.nanoTime()-startTime);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try(Scanner in = new Scanner(System.in)){
            System.out.println("Enter database url (support Oracle)");
            String url = in.nextLine();
            System.out.println("Enter user name");
            String username = in.nextLine();
            System.out.println("Enter user password");
            String password = in.nextLine();
            System.out.println("Enter table name (with schema)");
            String table = in.nextLine();
            System.out.println("Enter column with jar name");
            String jarName = in.nextLine();
            System.out.println("Enter column with file name");
            String fileName = in.nextLine();
            System.out.println("Enter column with package name");
            String packageName = in.nextLine();
            System.out.println("Enter column with BLOB");
            String blobName = in.nextLine();
            System.out.println("Enter jar name (if empty - all jars will be downloaded)");
            String purposeJar = in.nextLine();

            String jarListSQL = purposeJar!= "" ?
                    String.format("SELECT DISTINCT %s FROM %s WHERE %s = '%s.jar'", jarName, fileName, jarName, purposeJar.replace(".jar", ""))
                    : String.format("SELECT DISTINCT %s FROM %s", jarName, fileName);

            String classesSql = String.format("SELECT\n" +
                    "        %s,\n" +
                    "        %s,\n" +
                    "        %s,\n" +
                    "        %s\n" +
                    "FROM %s WHERE %s=?", jarName, fileName, packageName, blobName, table, jarName);

            readJars(url, username, password, jarListSQL, classesSql);

        }
        catch (Exception ex) {
            System.out.println("Error. Try again!");
            ex.printStackTrace();
        }
    }

    public static byte[] inflate(byte[] in) {
        if (in.length < 8) {
            throw new IllegalArgumentException("Input byte array must be at least 8 bytes.");
        } else if (in[3] != 55) {
            throw new IllegalArgumentException("Input byte array is not recognized as a supported version.");
        } else {
            Inflater inflater = null;
            byte[] var;
            try {
                int expectedSize = ((in[4] & 255) << 24) + ((in[5] & 255) << 16) + ((in[6] & 255) << 8) + (in[7] & 255);
                byte[] out = new byte[expectedSize];
                inflater = new Inflater();
                inflater.setInput(in, 8, in.length - 8);
                int size = inflater.inflate(out);
                if (size < expectedSize || !inflater.finished()) {
                    if (inflater.needsDictionary()) {
                        System.out.println("Inflate dictionary is required");
                    }
                    throw new RuntimeException("Truncated binary stream encountered. Expected decompressed size of " + expectedSize + ", but actually got " + size);
                }
                var = out;
            } catch (Exception exp) {
                throw new RuntimeException("Error occurred when decompressing binary stream data", exp);
            } finally {
                if (inflater != null) {
                    inflater.end();
                }
            }
            return var;
        }
    }
}
