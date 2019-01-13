package bean;

import lotus.domino.*;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.*;

@ApplicationScoped
@Named("dominoInfo")
public class DominoBean {
    public static final boolean IS_DOMINO;
    static {
        boolean isDomino;
        try {
            Class.forName("lotus.domino.NotesThread");
            isDomino = true;
        } catch(Throwable t) {
            isDomino = false;
        }

        IS_DOMINO = isDomino;
    }

    private static class DominoThreadFactory implements ThreadFactory {
        private static int spawnCount = 0;
        private static final Object sync = new Object();

        static final DominoThreadFactory instance = new DominoThreadFactory();

        static final ExecutorService executor = Executors.newCachedThreadPool(instance);

        @Override
        public Thread newThread(final Runnable runnable) {
            synchronized(sync) {
                spawnCount++;
            }
            try {
                return new NotesThread(runnable, "DominoThreadFactory Thread " + spawnCount);
            } catch(Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @PreDestroy
    public void term() {
        if(!IS_DOMINO) {
            return;
        }

        DominoThreadFactory.executor.shutdownNow();
        try {
            DominoThreadFactory.executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // That's fine
        }
    }

    public String getServer() throws ExecutionException, InterruptedException {
        if(!IS_DOMINO) {
            return null;
        }

        return DominoThreadFactory.executor.submit(() -> {
            try {
                Session session = NotesFactory.createSession();
                try {
                    return session.getUserName();
                } finally {
                    session.recycle();
                }
            } catch (Throwable t) {
                return t.getMessage();
            }
        }).get();
    }

    public String getVersion() throws ExecutionException, InterruptedException {
        if(!IS_DOMINO) {
            return null;
        }

        return DominoThreadFactory.executor.submit(() -> {
            try {
                Session session = NotesFactory.createSession();
                try {
                    return session.getNotesVersion();
                } finally {
                    session.recycle();
                }
            } catch (Throwable t) {
                return t.getMessage();
            }
        }).get();
    }

    public String getServerName() throws ExecutionException, InterruptedException {
        if(!IS_DOMINO) {
            return null;
        }

        return DominoThreadFactory.executor.submit(() -> {
            try {
                Session session = NotesFactory.createSession();
                try {
                   return session.getUserName();
                } finally {
                    session.recycle();
                }
            } catch(Throwable t) {
                return t.getMessage();
            }
        }).get();
    }
}
