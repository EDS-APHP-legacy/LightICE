package common.time;

public class Metronome extends WallClock {

    final long updatePeriod;

    public Metronome(long p) {
        updatePeriod = p;
    }

    @Override
    protected long getTimeInMillis() {
        long now = System.currentTimeMillis();
        now = now - now % updatePeriod;
        return now;
    }
}