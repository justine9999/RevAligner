package revaligner.service;

import java.io.Serializable;
import java.util.HashMap;
import javax.servlet.http.HttpSession;

public class SessionCollector
  implements Serializable
{
  private HashMap<HttpSession, String> sessionMap = new HashMap();
  private HashMap<HttpSession, Long> accessTimeMap = new HashMap();
  private HashMap<String, Integer> alignProgressMap = new HashMap();
  
  public HashMap<HttpSession, String> getSessionMap()
  {
    return this.sessionMap;
  }
  
  public HashMap<HttpSession, Long> getAccessTimeMap()
  {
    return this.accessTimeMap;
  }
  
  public HashMap<String, Integer> getAlignProgressMap()
  {
    return this.alignProgressMap;
  }
  
  public void removeSession(HttpSession httpSession){
	if(this.sessionMap.containsKey(httpSession)) this.sessionMap.remove(httpSession);
	if(this.accessTimeMap.containsKey(httpSession)) this.accessTimeMap.remove(httpSession);
  }
}
