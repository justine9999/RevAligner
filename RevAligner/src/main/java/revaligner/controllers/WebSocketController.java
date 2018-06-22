package revaligner.controllers;

import java.io.Serializable;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import revaligner.service.ProjectManager;
import revaligner.service.SessionCollector;

@Controller
public class WebSocketController
  implements Serializable
{
  @Inject
  SessionCollector sessionCollector;
  @Autowired
  SimpMessagingTemplate template;
  
  @MessageMapping({"/getalignprogress"})
  public void getAlignProgress(String prjid)
    throws Exception
  {
	  System.out.println(prjid);
    this.sessionCollector.getAlignProgressMap().put(prjid, 0);
    int prev = -1;
    while ((this.sessionCollector.getAlignProgressMap().get(prjid) <= 100) || (this.sessionCollector.getAlignProgressMap().get(prjid) >= 200))
    {
      int alignprogress = this.sessionCollector.getAlignProgressMap().get(prjid);
      if (alignprogress == -1) {
        break;
      }
      if (alignprogress != -2) {
        if (alignprogress >= 200)
        {
          this.template.convertAndSend("/topic/" + prjid, Integer.valueOf(alignprogress));
          this.sessionCollector.getAlignProgressMap().put(prjid, prev);
        }
        else
        {
          if (alignprogress > prev)
          {
            this.template.convertAndSend("/topic/" + prjid, Integer.valueOf(alignprogress));
            prev = alignprogress;
          }
          if (alignprogress == 100)
          {
        	  this.sessionCollector.getAlignProgressMap().remove(prjid);
            break;
          }
        }
      }
    }
  }
}
