package revaligner.service;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import revaligner.applicationconfiguration.BaseAppConfig;


@Component
public class JobCleaner {
	
	@Inject
	SessionCollector sessionCollector;
	
	@Autowired
	private BaseAppConfig baseAppConfig;
	
	@Scheduled(fixedRateString = "${revaligner.jobcleaninterval}"+"000")
    public void cleanJobs() {
		try{
		System.out.println("======================================================================>");
		System.out.println("checking if projects are still alive..start : " + this.sessionCollector.getSessionMap().size() + " projects alive");
		Iterator<Map.Entry<HttpSession, Long>> itr = this.sessionCollector.getAccessTimeMap().entrySet().iterator();
        while(itr.hasNext()){
        	Map.Entry<HttpSession, Long> entry = (Map.Entry<HttpSession, Long>)itr.next();
        	HttpSession httpSession = entry.getKey();
        	Long last_access_time = entry.getValue();

            System.out.println("project: " + this.sessionCollector.getSessionMap().get(httpSession).split("_")[1]);
            double timeoutinterval = this.baseAppConfig.getTIMEOUTINTERVAL();
            long currenttime = System.nanoTime();
            long elapsedTime = currenttime - last_access_time;
            double totalSeconds = elapsedTime / 1000000000.0D;
            System.out.println("idle for : " + totalSeconds + " / " + timeoutinterval + " second(s)");
            if (totalSeconds > timeoutinterval) {
            	System.out.println("- expired");
            	this.sessionCollector.removeSession(httpSession);
            }else{
            	System.out.println("+ alive");
            }
        }
        System.out.println("checking if projects are still alive..end : " + this.sessionCollector.getSessionMap().size() + " projects alive");
        System.out.println("<======================================================================");
		}catch(Exception ex){
			ex.printStackTrace();
		}
    }
}
