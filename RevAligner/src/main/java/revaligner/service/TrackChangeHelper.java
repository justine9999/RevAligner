package revaligner.service;

import java.io.Serializable;

public class TrackChangeHelper
  implements Serializable
{
  public static int getTrackChangeType(String s)
  {
    s = s.replace("</del><del>", "").replace("</ins><ins>", "");
    if ((s.contains("<ins>")) || (s.contains("<del>")))
    {
      if (s.split("<(/)*ins>|<(/)*del>").length == 2)
      {
        if (s.startsWith("<ins>")) {
          return 1;
        }
        if (s.startsWith("<del>")) {
          return 2;
        }
        return 3;
      }
      return 3;
    }
    return 0;
  }
  
  public static String[] getTxlfTrgStatsFromTCType(String tctype, String trgtext)
  {
    if (tctype.equals("NONE"))
    {
      if (trgtext.trim().equals("")) {
        return new String[] { "0", "needs-translation", "x-no-match" };
      }
      return new String[] { "1", "translated", "exact-match" };
    }
    if (tctype.equals("INSERTION")) {
      return new String[] { "0", "needs-translation", "x-no-match" };
    }
    if (tctype.equals("DELETION")) {
      return new String[] { "N/A", "N/A", "N/A" };
    }
    if (trgtext.trim().equals("")) {
      return new String[] { "0", "needs-translation", "x-no-match" };
    }
    return new String[] { "75", "needs-translation", "fuzzy-match" };
  }
}
