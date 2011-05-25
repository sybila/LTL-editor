//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

import java.util.ArrayList;
import java.util.List;

/**
 * Time series: a sequence of a species concentrations and its derivatives measured at multiples of a time interval.
 * 
 * Used as an underlying data structure for the time series which is annotated by user. Is not supposed to change after loading.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class TimeSeries {
	/**
	 * Basic private implementation of {@link TimeSeriesIterator}.
	 * 
	 * @author Tomáš Vejpustek
	 */
	private class TimeSeriesIteratorImpl implements TimeSeriesIterator {
		private TimeSeries parent;
		private List<TimeSeriesPoint> series;
		int index;
		
		/**
		 * Constructs iterator over given series. Used by {@link TimeSeries#getIterator()}.
		 * 
		 * @param parent Parent series.
		 * @param series Inner list of points of parent series.
		 */
		public TimeSeriesIteratorImpl(TimeSeries parent, List<TimeSeriesPoint> series) {
			this.parent = parent;
			this.series = series;
			reset();
		}
		
		@Override
		public void reset() {
			index = -1;
		}
		
		@Override
		public boolean hasNext() {
			return (series.size() > index+1);
		}
		
		@Override
		public TimeSeriesPoint next() {
			if (hasNext()) {
				index++;
				return getPoint();
			} else {
				return null;
			}
		}	
		
		@Override
		public TimeSeriesPoint getPoint() {
			return series.get(index);
		}

		@Override
		public double getTime() {
			return index*parent.getInterval();
		}
	}
	
	private List<TimeSeriesPoint> points = new ArrayList<TimeSeriesPoint>();
	private double interval;

	/**
	 * Creates an empty time series.
	 */
	public TimeSeries() {
		interval = 0;
	}

	/**
	 * @return <code>true</code> when there is no point in this time series, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return points.isEmpty();
	}
	
	/**
	 * Loads time series from given source (usually file).
	 * @param source Wrapper of given input.
	 * @throws TSLoaderException when IO error during loading is encountered.
	 */
	public TimeSeries(TimeSeriesLoader source) throws TSLoaderException {
		interval = source.getInterval();
		
		TimeSeriesPoint input;
		while (null != (input = source.readPoint())) {
			points.add(input);
		}
	}
	/**
	 * @return Length of time interval between consecutive points.
	 */
	public double getInterval() {
		return interval;
	}
	
	/**
	 * @return Iterator over time series.
	 */
	public TimeSeriesIterator getIterator() {
		return new TimeSeriesIteratorImpl(this, points);
	}
	
	/**
	 * @return Duration of time series, i.e. the interval between its first and last points.
	 */
	public double getLength() {
		return points.size()*getInterval();
	}
}
