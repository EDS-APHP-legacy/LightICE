package runner.philips;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.AbstractDeviceRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class SampleCache {
    protected static final Logger log = LoggerFactory.getLogger(AbstractDeviceRunner.class);

    private final List<Number> newSamples = Collections.synchronizedList(new ArrayList<Number>());
    private final List<Number> oldSamples = Collections.synchronizedList(new ArrayList<Number>());

    public void addNewSamples(Collection<Number> coll) {
        newSamples.addAll(coll);
    }

    public Collection<Number> emitSamples(int n, String s) {
        synchronized(newSamples) {
            if(newSamples.isEmpty()) {
                log.warn(s+" no new samples to emit");
                return null;
            }

            if(newSamples.size() < n) {
                log.warn(s+" will repeat " + (n - newSamples.size()) + " old samples to make up a shortfall");
            }
            // Move up to n samples from the old list to the new
            List<Number> oldestNewSamples = newSamples.subList(0, n > newSamples.size() ? newSamples.size() : n);

            oldSamples.addAll(oldestNewSamples);
            oldestNewSamples.clear();
        }
        synchronized(oldSamples) {
            // If we have insufficient oldSamples (shouldn't happen except maybe at initialization) fill in values
            if(oldSamples.size() < n) {
                log.warn(s+" filling in " + (n - oldSamples.size()) + " zeros; this should not continue happening");
                while(oldSamples.size() < n) {
                    oldSamples.add(0, 0);
                }
            }
            // If we have extra oldSamples then remove them
            if(oldSamples.size() > n) {
                oldSamples.subList(0, oldSamples.size()-n).clear();
            }
        }
        return oldSamples;
    }
}