package edu.cmu.hcii.hasd;

/**
 * A class that encapsulates all data associated with one line in the
 * editor. This means that when lines are added, removed, etc. the right
 * data stays with each line.
 *
 * @author William Choi
 */

public class HASDLineModel {
    public boolean isPrintPoint;
    public boolean executedInLastRun;
    public boolean isMostRecentlyExecuted;

    public int countAtLastExec;

    public HASDLineModel() {
            isPrintPoint = false;
            executedInLastRun = false;
            isMostRecentlyExecuted = false;
    }
}
