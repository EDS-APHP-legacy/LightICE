package runner.philips;

import common.time.IceInstant;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.AbstractDeviceRunner;

import java.util.*;

public final class SampleCache {
    protected static final Logger log = LoggerFactory.getLogger(AbstractDeviceRunner.class);

    private final List<Number> valueCache = Collections.synchronizedList(new ArrayList<>());
    private IceInstant startTime = null;

    public void addNewSamples(Collection<Number> coll, IceInstant deviceTime) {
        synchronized (valueCache) {
            if (startTime == null)
                startTime = deviceTime;
            valueCache.addAll(coll);
        }
    }

    public Pair<IceInstant,List<Number>> emitSamples(String s) {
        synchronized (valueCache) {
            if (valueCache.isEmpty())
                return null;
            List<Number> values = new ArrayList<>(valueCache);
            valueCache.clear();

            Pair<IceInstant, List<Number>> result = new Pair<>(startTime, values);
            startTime = null;

            return result;
        }
    }
}