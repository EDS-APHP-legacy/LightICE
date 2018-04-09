package runner.philips;

import drivers.philips.intellivue.data.SampleArraySpecification;
import drivers.philips.intellivue.data.ScaleAndRangeSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.AbstractDeviceRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MySampleArray {
    protected static final Logger log = LoggerFactory.getLogger(AbstractDeviceRunner.class);

    private short sampleSize, significantBits;
    private double lowerAbsoluteValue, upperAbsoluteValue;
    private int lowerScaledValue, upperScaledValue;
    private final List<Number> numbers = new ArrayList<>();

    public MySampleArray(SampleArraySpecification sas, ScaleAndRangeSpecification sar) {
        setSampleSize(sas.getSampleSize());
        setSignificantBits(sas.getSignificantBits());
        setArraySize(sas.getArraySize());
        buildMaskAndShift();
        this.lowerAbsoluteValue = sar.getLowerAbsoluteValue().doubleValue();
        this.upperAbsoluteValue = sar.getUpperAbsoluteValue().doubleValue();
        this.lowerScaledValue = sar.getLowerScaledValue();
        this.upperScaledValue = sar.getUpperScaledValue();
    }
    public List<Number> getNumbers() {
        return numbers;
    }

    private int[] mask = new int[0];
    private int[] shift = new int[0];

    public void applyValue(int sampleNumber, short[] values) {
        int value = 0;
        for (int i = 0; i < sampleSize; i++) {
            int idx = sampleNumber * sampleSize + i;
            if (idx >= values.length) {
                log.warn("Cannot locate index " + idx + " where values.length=" + values.length + " sampleSize=" + sampleSize + " sampleNumber="
                        + sampleNumber + " i=" + i);
            } else {
                if (i >= mask.length) {
                    log.warn("Cannot access i=" + i + " where mask.length=" + mask.length);
                } else {
                    if (i >= shift.length) {
                        log.warn("Cannot access i=" + i + " where shift.length=" + shift.length);
                    } else {
                        value |= (mask[i] & values[idx]) << shift[i];
                    }
                }
            }
        }
        if (sampleNumber >= numbers.size()) {
            log.warn("Received sampleNumber=" + sampleNumber + " where expected size was " + numbers.size());
        } else {
            // Scale and range the value

            if(!Double.isNaN(lowerAbsoluteValue) && !Double.isNaN(upperAbsoluteValue)) {
                if(upperScaledValue == lowerScaledValue) {
                    log.error("Not scaling " + value + " between scaled " + lowerScaledValue + " and " + upperScaledValue);
                } else {
                    double prop = 1.0 * (value - lowerScaledValue) / (upperScaledValue - lowerScaledValue);
                    if(lowerAbsoluteValue == upperAbsoluteValue) {
                        log.error("Not scaling " + value + " (proportionally " + prop+ ") between " + lowerAbsoluteValue + " and " + upperScaledValue);
                    } else {
                        prop = lowerAbsoluteValue + prop * (upperAbsoluteValue - lowerAbsoluteValue);
                        numbers.set(sampleNumber, prop);
                    }
                }
            } else {
                numbers.set(sampleNumber, value);
            }
        }
    }

    public static final int createMask(int prefix) {
        int mask = 0;

        for (int i = 0; i < prefix; i++) {
            mask |= (1 << i);
        }
        return mask;
    }

    private void buildMaskAndShift() {
        if (this.shift.length < sampleSize) {
            this.shift = new int[sampleSize];
        }
        if (this.mask.length < sampleSize) {
            this.mask = new int[sampleSize];
        }
        int significantBits = this.significantBits;
        for (int i = sampleSize - 1; i >= 0; i--) {
            shift[i] = (sampleSize - i - 1) * Byte.SIZE;
            mask[i] = significantBits >= Byte.SIZE ? 0xFF : createMask(significantBits);
            significantBits -= Byte.SIZE;
        }
        log.debug("Mask:" + Arrays.toString(mask) + " Shift:" + Arrays.toString(shift) + " sampleSize=" + sampleSize + " sigBits="
                + this.significantBits);
    }

    public short getSampleSize() {
        return sampleSize;
    }

    public short getSignificantBits() {
        return significantBits;
    }

    public void setSampleSize(short s) {
        s /= Byte.SIZE;
        if (sampleSize != s) {
            this.sampleSize = s;
            buildMaskAndShift();
        }
    }

    public void setSignificantBits(short s) {
        if (significantBits != s) {
            this.significantBits = s;
            buildMaskAndShift();
        }
    }

    public int getArraySize() {
        return numbers.size();
    }

    public void setArraySize(int size) {
        if (size != numbers.size()) {
            while (numbers.size() < size) {
                numbers.add(0);
            }
            while (numbers.size() > size) {
                numbers.remove(0);
            }
        }
    }

}