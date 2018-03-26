package revaligner.applicationconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class BaseAppConfig
  implements Serializable
{
private final String BASE_WORKING_DIR;
  private final int TIME_OUT_INTERVAL;
  private final int TIME_OUT_CHECK_INTERVAL;
  private final int AUTO_SAVE_INTERVAL;
  static final Logger logger = Logger.getLogger(BaseAppConfig.class);
  
  public BaseAppConfig()
  {
    Properties props = new Properties();
    try
    {
      InputStream in = getClass().getClassLoader().getResourceAsStream("BaseConfiguration.properties");
      props.load(in);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    this.BASE_WORKING_DIR = props.getProperty("revaligner.baseworkingdir");
    this.TIME_OUT_INTERVAL = Integer.parseInt(props.getProperty("revaligner.timeoutinterval"));
    this.TIME_OUT_CHECK_INTERVAL = Integer.parseInt(props.getProperty("revaligner.timeoutcheckinterval"));
    this.AUTO_SAVE_INTERVAL = Integer.parseInt(props.getProperty("revaligner.autosaveinterval"));
    
    RollingFileAppender appender = new RollingFileAppender();
    appender.setAppend(true);
    appender.setMaxFileSize("5MB");
    appender.setMaxBackupIndex(10);
    appender.setName("main");
    appender.setFile(props.getProperty("revaligner.mainlogdir"));
    
    PatternLayout layOut = new PatternLayout();
    layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
    appender.setLayout(layOut);
    
    appender.activateOptions();
    logger.addAppender(appender);
    logger.setLevel(Level.ERROR);
  }
  
  public String getBASEWORKINGDIR()
  {
    return this.BASE_WORKING_DIR;
  }
  
  public Logger getMAINLOGGER()
  {
    return logger;
  }
  
  public double getTIMEOUTINTERVAL()
  {
    return this.TIME_OUT_INTERVAL;
  }
  
  public double getTIMEOUTCHECKINTERVAL()
  {
    return this.TIME_OUT_CHECK_INTERVAL;
  }
  
  public double getAUTOSAVEINTERVAL()
  {
    return this.AUTO_SAVE_INTERVAL;
  }
}
