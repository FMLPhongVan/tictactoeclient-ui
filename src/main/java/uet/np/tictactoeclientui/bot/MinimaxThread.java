package uet.np.tictactoeclientui.bot;

public class MinimaxThread extends Thread {
    public MinimaxRunnable minimaxRunnable = null;

    public MinimaxThread(MinimaxRunnable runnable) {
        super(runnable);
        minimaxRunnable = runnable;
    }
}

