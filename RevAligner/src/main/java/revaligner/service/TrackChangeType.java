package revaligner.service;

import java.io.Serializable;

public final class TrackChangeType
  implements Serializable
{
  public static final int NA = -1;
  public static final int NONE = 0;
  public static final int INSERTION = 1;
  public static final int DELETION = 2;
  public static final int MIX = 3;
  public static final int FORMATTING = 4;
  
  public static String getName(int TCType)
  {
    switch (TCType)
    {
    case -1: 
      return "N/A";
    case 0: 
      return "NONE";
    case 1: 
      return "INSERTION";
    case 2: 
      return "DELETION";
    case 3: 
      return "MIX";
    case 4: 
      return "FORMATTING";
    }
    return "UNKNOWN";
  }
}
