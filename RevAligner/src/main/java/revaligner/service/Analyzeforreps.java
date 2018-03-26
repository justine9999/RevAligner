package revaligner.service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.gs4tr.tm3.commandline.utils.XliffAnalyzer;
import org.gs4tr.tm3.statistics.StatisticsImpl;

class Analyzeforreps
  implements Runnable
{
  private Thread thread;
  private List<String> fileList;
  private String threadName;
  private XliffAnalyzer analyzer;
  private boolean forceReanalysis;
  @SuppressWarnings("unused")
private StatisticsImpl stats;
  private CountDownLatch latch;
  
  Analyzeforreps(String tName, XliffAnalyzer analyzer, List<String> files, boolean forceReanalysis, StatisticsImpl stats, CountDownLatch latch)
  {
    this.threadName = tName;
    this.fileList = files;
    this.analyzer = analyzer;
    this.forceReanalysis = forceReanalysis;
    this.stats = stats;
    this.latch = latch;
  }
  
  public void run()
  {
    try
    {
      process();
      this.latch.countDown();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void start()
  {
    if (this.thread == null)
    {
      this.thread = new Thread(this, this.threadName);
      this.thread.start();
    }
  }
  
  public void process()
    throws Exception
  {
    this.stats = this.analyzer.analyze(this.fileList, this.forceReanalysis);
  }
}
