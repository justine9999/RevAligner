package revaligner.domain;

import java.io.File;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import revaligner.applicationconfiguration.BaseAppConfig;


public class RevProject
  implements Serializable
{
  @Autowired
  private BaseAppConfig baseAppConfig;
  private String prjNumber;
  private String prjSubName;
  private String prjCreationDate;
  private String srcLang;
  private String trgLang;
  private String username;
  
  public BaseAppConfig getBaseAppConfig()
  {
    return this.baseAppConfig;
  }
  
  public String getPrjNumber()
  {
    return this.prjNumber;
  }
  
  public void setPrjNumber(String prjNumber)
  {
    this.prjNumber = prjNumber;
  }
  
  public String getPrjSubName()
  {
    return this.prjSubName;
  }
  
  public void setPrjSubName(String prjSubName)
  {
    this.prjSubName = prjSubName;
  }
  
  public String getPrjCreationDate()
  {
    return this.prjCreationDate;
  }
  
  public void setPrjCreationDate(String prjCreationDate)
  {
    this.prjCreationDate = prjCreationDate;
  }
  
  public String getSrcLang()
  {
    return this.srcLang;
  }
  
  public void setSrcLang(String srcLang)
  {
    this.srcLang = srcLang;
  }
  
  public String getTrgLang()
  {
    return this.trgLang;
  }
  
  public void setTrgLang(String trgLang)
  {
    this.trgLang = trgLang;
  }
  
  public String getUsername()
  {
    return this.username;
  }
  
  public void setUsername(String username)
  {
    this.username = username;
  }
  
  public String getBaseWorkingPath()
  {
    return getBaseAppConfig().getBASEWORKINGDIR();
  }
  
  public Logger getMainLogger()
  {
    return getBaseAppConfig().getMAINLOGGER();
  }
  
  public double getTimeOutInterval()
  {
    return getBaseAppConfig().getTIMEOUTINTERVAL();
  }
  
  public double getTimeOutCheckInterval()
  {
    return getBaseAppConfig().getTIMEOUTCHECKINTERVAL();
  }
  
  public double getAutoSaveInterval()
  {
    return getBaseAppConfig().getAUTOSAVEINTERVAL();
  }
  
  public double getJobCleanInterval()
  {
    return getBaseAppConfig().getJOBCLEANINTERVAL();
  }
  
  public String getUserFolder()
  {
    return getBaseAppConfig().getBASEWORKINGDIR() + File.separator + this.username;
  }
  
  public String getPrjWorkingPath()
  {
    return getBaseAppConfig().getBASEWORKINGDIR() + File.separator + this.username + File.separator + this.prjNumber;
  }
  
  public String getProjectInfoFile()
  {
    return getPrjWorkingPath() + File.separator + "_.info";
  }
  
  public String getAlignedXmlPath()
  {
    return getPrjWorkingPath() + File.separator + "rev_aligned.xml";
  }
  
  public String getAutoSavedAlignedXmlPath()
  {
    return getPrjWorkingPath() + File.separator + "rev_aligned.xml.temp";
  }
  
  public String getExportZipFolderPath()
  {
    return getPrjWorkingPath() + File.separator + "export";
  }
  
  public String getTranslationKitPath()
  {
    return getPrjWorkingPath() + File.separator + "source_formatted";
  }
  
  public String getParagraphAlignmentFile()
  {
    return getPrjWorkingPath() + File.separator + "verifyParas.xlsx";
  }
}
