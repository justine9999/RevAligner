package revaligner.service;

import java.util.Comparator;

public class CreationDateComparator
  implements Comparator<String[]>
{
  public int compare(String[] x, String[] y)
  {
    long xx = Long.parseLong(x[1].replace("/", "").replace(":", "").replace("_", "").replace(" ", ""));
    long yy = Long.parseLong(y[1].replace("/", "").replace(":", "").replace("_", "").replace(" ", ""));
    if (xx < yy) {
      return 1;
    }
    if (xx > yy) {
      return -1;
    }
    return 0;
  }
}
