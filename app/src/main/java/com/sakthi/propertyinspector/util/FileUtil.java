package com.sakthi.propertyinspector.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by sakthivel on 5/27/2016.
 */
public class FileUtil {

    public static void createDirectory(String filePath){
        File dirFile=new File(filePath);
        dirFile.mkdirs();
    }

    public static  boolean isDirFound(String dirPath){
        File dirFile=new File(dirPath);
        return dirFile.exists();
    }


    public static boolean isValidPropertyFile(String filePath){

        String[] splittedFile=filePath.split("/");
        //create necessay directory in the app dir ( dir same as file name and inside another one images folder)

        String fileName=splittedFile[splittedFile.length-1];
        String[] fileInfo=fileName.split("_");

        // We have a version file so it is valid
        if (fileInfo.length == 4 && fileInfo[3].matches("\\d+\\.csv")) return true;

        //file name contains client id, property id and file version (if it not its not a valid csv file throw exception)
        if (fileInfo.length != 3 || !fileInfo[2].contains("ce")) return false;
        else return true;

    }

   /* public ArrayList<File> getInspectedPropertyFiles(String appDirPath){
        ArrayList<File> inspectedFiles=new ArrayList<>();

    }*/


    public static String[] isInspectedAlready(String filePath,String appDirPath,String defFileName){

        String[] splittedFile=filePath.split("/");
        //create necessay directory in the app dir ( dir same as file name and inside another one images folder)

        String fileName=splittedFile[splittedFile.length-1];
        String[] fileInfo=fileName.split("_");

        String workDirName=fileInfo[0]+"_"+fileInfo[1];
        String workDirPath=appDirPath+workDirName+"/";

        if(!FileUtil.isDirFound(workDirPath))return null;
        String inspectedFileName=workDirPath+workDirName+"_"+defFileName+".csv";

        return new File(inspectedFileName).exists()?new String[]{workDirPath,inspectedFileName}:null;

    }

    public static String[] getDirAndBaseFilePath(String versionFilePath) {
        String[] splittedFile = versionFilePath.split("/");
        String baseDirPath = "";
        for (int i = 0; i < splittedFile.length - 1; i++) {
            baseDirPath += splittedFile[i] + "/";
        }

        String fileName = splittedFile[splittedFile.length - 1];
        String[] fileInfo = fileName.split("_");

        String baseFilePath = "";
        if (fileInfo.length == 4) {
            baseFilePath = baseDirPath + fileInfo[0] + "_" + fileInfo[1] + "_" + fileInfo[2] + ".csv";
        } else {
            baseFilePath = baseDirPath + fileName;
        }

        return new String[]{baseDirPath, baseFilePath};
    }

    public static String[] initFilesForInspection(String filePath,String appDirPath,String defFileName) throws Exception{

        //check for file whether it is a valid csv file for not
        //split file name and create directory in application dir path

        String[] splittedFile=filePath.split("/");

        //create necessay directory in the app dir ( dir same as file name and inside another one images folder)

        String fileName=splittedFile[splittedFile.length-1];
        String[] fileInfo=fileName.split("_");

        //file name contains client id, property id and file version (if it not its not a valid csv file throw exception)
        if(fileInfo.length!=3||!fileInfo[2].contains("ce")){
            //throw Exception and return null
            throw new Exception("Pick valid property file");
        }

        String workDirName=fileInfo[0]+"_"+fileInfo[1];
        String workDirPath=appDirPath+workDirName+"/";
        String imgDirPath=workDirPath+"images/";

        if(!FileUtil.isDirFound(workDirPath)){
            FileUtil.createDirectory(workDirPath+"images/");
        }

        int indxOfDot=fileInfo[2].indexOf(".");
        String txtVer=fileInfo[2].substring(0,indxOfDot).substring(2);

        int version=ParseUtil.getIntFromString(txtVer,1);
        version+=1;

        //create next version file on that directory
        String newFileVersion=workDirPath+workDirName+"_"+defFileName+".csv";//"_ce"+(version<=9?("0"+version):(""+version))+".csv";
        createFile(newFileVersion);

        Log.e("File Util workDir ",workDirPath);
        Log.e("File Util file version",newFileVersion);

        return new String[]{workDirPath,newFileVersion};
    }

    public static String getDirectoryName(String filePath){
        return null;
    }

    public static String getParentDirectory(String path){
        File file=new File(path);
        return file.getParentFile().getAbsolutePath()+"/";
    }

    public static void createFile(String path) throws IOException {

        File file=new File(path);
        if(!file.exists())file.createNewFile();

    }

    public static void copyFile(String fromFile,String toFile){



    }

    public static void copyVersionToBase(String versionFilePath, String baseFilePath) {
        if (versionFilePath.equals(baseFilePath)) return;

        File versionFile = new File(versionFilePath);
        File baseFile = new File(baseFilePath);
        File tempFile = new File(baseFilePath + "_to_be_removed");

        baseFile.renameTo(tempFile);
        versionFile.renameTo(baseFile);

        if (tempFile.exists()) tempFile.delete();
    }

    public static void removeVersionFiles(String dirPath, String baseFilePath) {
        String baseFileParts[] = baseFilePath.split("/");
        if (baseFileParts.length == 0) return;
        final String baseFileName = baseFileParts[baseFileParts.length - 1].split(".csv")[0];

        File baseDir = new File(dirPath);
        if (baseDir == null) return;

        // Get all version files
        final File[] files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename == null) return false;

                return filename.matches(baseFileName + "_\\d+\\.csv");
            }
        });

        // Remove all version files
        if (files != null) {
            for (File file : files) {
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        }
    }


    public static File[] getImageFiles(String dirPath, final String filter){
        ArrayList<File> files=new ArrayList<>();

        File dirFile=new File(dirPath);
        FilenameFilter nameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                //Log.e("File available:"+filter,dir.getName()+" : "+filename);
                boolean isFiltered= filter!=null&&filename.startsWith(filter);

                if(filter==null){
                    return filename.endsWith(".jpg");
                }else return isFiltered;



            }
        };

        File[] listFile=dirFile.listFiles(nameFilter);
        return listFile;
    }

    public static void deleteFile(String filePath){
        File file=new File(filePath);
        if(file.exists())file.delete();
    }


    public static void saveCrashReport(Context context,String report){
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = new AppPreference(context).getAppDirPath(); //Environment.getExternalStorageDirectory() + "/" + "In/";
        String filePath = path +"crash_report.txt";

        final Intent result = new Intent(android.content.Intent.ACTION_SEND);
        result.setType("plain/text");
        result.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "sakthi8fivevel@gmail.com" });
        result.putExtra(android.content.Intent.EXTRA_SUBJECT, "TLS Property Inspector android crash report");
        result.putExtra(android.content.Intent.EXTRA_TEXT, filePath);
        context.startActivity(Intent.createChooser(result,"Send Report"));

       /* File file=new File(filePath);
        if(!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream ostream=null;
        try {
             ostream=new FileOutputStream(filePath);
            ostream.write(report.getBytes());
            ostream.flush();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }finally {
            if(ostream!=null) try {
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

      /*  String[] cmd = new String[] {"logcat","-f",filePath,"-v","time","<MyTagName>:D","*:S"};
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/
        // Extract to file.
        File file = new File (filePath);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try
        {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader (process.getInputStream());

            // write output stream
            writer = new FileWriter (file);
            writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
            writer.write ("Device: " + model + "\n");
            writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do
            {
                int n = reader.read (buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write (buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        }
        catch (IOException e)
        {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return;
        }

        return ;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static int clearAllInspectionFolders(Context context) {
        AppPreference appPreference = new AppPreference(context);
        File appDir = new File(appPreference.getAppDirPath());

        appPreference.setInspectedProperty(null);

        return deleteFilesRecursive(appDir);
    }

    public static int deleteFilesRecursive(File fileOrDirectory) {
        int deletedFiles = 0;
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deletedFiles += deleteFilesRecursive(child);
            }
        }

        deletedFiles += (fileOrDirectory.delete() ? 1 : 0);

        return deletedFiles;
    }

    // test case

    // if dir chosed in sd card and sd card removed
    // if working dir is in sd card or dir deleted

}
