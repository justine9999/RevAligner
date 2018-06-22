package revaligner.applicationconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import revaligner.domain.RevProject;
import revaligner.service.ProjectManager;


@Configuration
@PropertySource("classpath:BaseConfiguration.properties")
public class BaseAppConfig
  implements Serializable
{
	
  @Value("${revaligner.baseworkingdir}")
  private  String BASE_WORKING_DIR;
  
  @Value("${revaligner.timeoutinterval}")
  private  int TIME_OUT_INTERVAL;
  
  @Value("${revaligner.timeoutcheckinterval}")
  private  int TIME_OUT_CHECK_INTERVAL;
  
  @Value("${revaligner.autosaveinterval}")
  private  int AUTO_SAVE_INTERVAL;
  
  @Value("${revaligner.jobcleaninterval}")
  private  int JOB_CLEAN_INTERVAL;
  
  static final Logger logger = Logger.getLogger(BaseAppConfig.class);
  
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
	  return new PropertySourcesPlaceholderConfigurer();
  }
  
  /*@PostConstruct
  public void init(){
      System.out.println("!!!!!!!!!!!!!!!!!"+ this.BASE_WORKING_DIR);
  }*/
  
  @Bean
  @Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
  public RevProject revProject(){
     return new RevProject();
  }
  
  @Bean
  @Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
  public ProjectManager projectManager(){
     return new ProjectManager();
  }
  
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
    
    //this.BASE_WORKING_DIR = props.getProperty("revaligner.baseworkingdir");
    //this.TIME_OUT_INTERVAL = Integer.parseInt(props.getProperty("revaligner.timeoutinterval"));
    //this.TIME_OUT_CHECK_INTERVAL = Integer.parseInt(props.getProperty("revaligner.timeoutcheckinterval"));
    //this.AUTO_SAVE_INTERVAL = Integer.parseInt(props.getProperty("revaligner.autosaveinterval"));
    //this.JOB_CLEAN_INTERVAL = Integer.parseInt(props.getProperty("revaligner.jobcleaninterval"));
    
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
  
  public double getJOBCLEANINTERVAL()
  {
    return this.JOB_CLEAN_INTERVAL;
  }
}
