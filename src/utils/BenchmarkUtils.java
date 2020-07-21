package utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class BenchmarkUtils {
	
	public enum Status {
		SOLVED, MEMORY_LIMIT_REACHED, TIMEOUT, UNEXPECTED
	}
	
	public int getIndicatorBasedOnStatus(Status status) {
		if (status.equals(Status.MEMORY_LIMIT_REACHED)) {
			return BenchmarkConstants.MEMORY_LIMIT_REACHED_INDICATOR;
		} else {
			return 0;
		}
		
	}

	public static long getDurationNano(long startNano, long endNano) {
		return endNano - startNano;
	}
	
	/**
	 * Divides two big integers result with up to 5 digits
	 * @param int1
	 * @param int2
	 * @return 
	 */
	public BigDecimal divideBigIntegers(BigInteger int1, BigInteger int2) {
		return new BigDecimal(int1).divide(new BigDecimal(int2), 5, RoundingMode.HALF_EVEN);
	}
	
	public static float translateToTimeUnit(float time, TimeUnit timeUnit) {
		return (float) time / TimeUnit.NANOSECONDS.convert(1, timeUnit);
	}
	
	public static String getTimeUnitAttachment(TimeUnit unit) {
		return "(" + unit.name() + ")";
	}
}
