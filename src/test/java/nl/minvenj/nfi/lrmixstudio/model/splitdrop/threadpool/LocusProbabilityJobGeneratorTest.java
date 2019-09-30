/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;

/**
 *
 * @author dejong
 */
public class LocusProbabilityJobGeneratorTest {

    public LocusProbabilityJobGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of generate method, of class LocusProbabilityJobGenerator.
     */
    @Test
    public void testGenerate() {
        System.out.println("generate");
        String locusName = "";
        Collection<Sample> activeReplicates = new ArrayList<>();
        activeReplicates.add(new Sample("R1"));
        Hypothesis hypothesis = new Hypothesis("Dummy", new PopulationStatistics("bla"));
        AnalysisProgressListener progress = null;
        Collection<LocusProbabilityJob> jobs = LocusProbabilityJobGenerator.generate(locusName, activeReplicates, hypothesis, progress);
        assertTrue(jobs.size() > 0);
    }

    private static class FutureImpl implements Future {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return "";
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return "";
        }
    }

    private static class ExecutionService implements ExecutorService {

        private int size;

        public int size() {
            return size;
        }

        public ExecutionService() {
            size = 0;
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return true;
        }

        @Override
        public boolean isTerminated() {
            return true;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            size++;
            return new FutureImpl();
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return new FutureImpl();
        }

        @Override
        public Future<?> submit(Runnable task) {
            return new FutureImpl();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return new ArrayList<>();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return new ArrayList<>();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void execute(Runnable command) {
        }
    }
}
