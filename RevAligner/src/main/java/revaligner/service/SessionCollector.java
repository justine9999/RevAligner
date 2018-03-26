package revaligner.service;

import java.io.Serializable;
import java.util.HashMap;
import javax.servlet.http.HttpSession;

public class SessionCollector
  implements Serializable
{
  private HashMap<HttpSession, String> sessionMap = new HashMap();
  
  public HashMap<HttpSession, String> getSessionMap()
  {
    return this.sessionMap;
  }
}
