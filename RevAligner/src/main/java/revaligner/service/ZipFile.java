package revaligner.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;

public class ZipFile
  implements Serializable
{
  List<File> filesToAdd = new ArrayList();
  
  public void ZipIt(String zipFile, String zipFolder)
    throws Exception
  {
    ZipOutputStream outputStream = null;
    InputStream inputStream = null;
    
    List<File> arrLocal = ExploredFolder(zipFolder);
    
    outputStream = new ZipOutputStream(new FileOutputStream(new File(zipFile)));
    
    ZipParameters parameters = new ZipParameters();
    parameters.setCompressionMethod(8);
    parameters.setDefaultFolderPath(zipFolder);
    parameters.setCompressionLevel(5);
    for (int i = 0; i < arrLocal.size(); i++)
    {
      File file = (File)arrLocal.get(i);
      outputStream.putNextEntry(file, parameters);
      if (file.isDirectory())
      {
        outputStream.closeEntry();
      }
      else
      {
        inputStream = new FileInputStream(file);
        byte[] readBuff = new byte[4096];
        int readLen = -1;
        while ((readLen = inputStream.read(readBuff)) != -1) {
          outputStream.write(readBuff, 0, readLen);
        }
        outputStream.closeEntry();
        inputStream.close();
      }
    }
    outputStream.finish();
    if (outputStream != null) {
      try
      {
        outputStream.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    if (inputStream != null) {
      try
      {
        inputStream.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  private List<File> ExploredFolder(String sAbsolutePath)
  {
    File fsSelectedPath = new File(sAbsolutePath);
    File[] sfiles = fsSelectedPath.listFiles();
    if (sfiles == null) {
      return null;
    }
    for (int j = 0; j < sfiles.length; j++)
    {
      File f = sfiles[j];
      if (f.isDirectory()) {
        ExploredFolder(f.getAbsolutePath());
      } else {
        this.filesToAdd.add(f);
      }
    }
    return this.filesToAdd;
  }
}
