package revaligner.controllers;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import revaligner.service.ProjectManager;

@Controller
public class WebSocketController
  implements Serializable
{
ProjectManager projectManager = new ProjectManager();
  @Autowired
  SimpMessagingTemplate template;
  
  @MessageMapping({"/getalignprogress"})
  public void getAlignProgress(String prjid)
    throws Exception
  {
    //long starttime = System.nanoTime();
    this.projectManager.setAlignProgress(0, prjid);
    int prev = -1;
    while ((this.projectManager.getAlignProgress(prjid) <= 100) || (this.projectManager.getAlignProgress(prjid) >= 200))
    {
      int alignprogress = this.projectManager.getAlignProgress(prjid);
      if (alignprogress == -1) {
        break;
      }
      if (alignprogress != -2) {
        if (alignprogress >= 200)
        {
          this.template.convertAndSend("/topic/" + prjid, Integer.valueOf(alignprogress));
          this.projectManager.setAlignProgress(prev, prjid);
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
            this.projectManager.removeAlignProgressRecord(prjid);
            break;
          }
        }
      }
    }
  }
}
