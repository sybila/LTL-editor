//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package series_new;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A sequence of {@link TimeSeriesPoint} in ascending time order.
 * 
 * Underlying data structure to be annotated by a user. Not supposed to change after loading.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class TimeSeries {
	private List<TimeSeriesPoint> points = new ArrayList<TimeSeriesPoint>();
	
	private class TimeSeriesIteratorImpl implements TimeSeriesIterator {
		private ListIterator<TimeSeriesPoint> iter;
		
		public TimeSeriesIteratorImpl(ListIterator<TimeSeriesPoint> iterator) {
			iter = iterator;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public TimeSeriesPoint next() {
			return iter.next();
		}
	}
	
	/**
	 * Creates empty time series.
	 */
	public TimeSeries() {}
	
	/**
	 * Loads time series from given source (usually a file).
	 * @param source Wrapper of designated input.
	 * @throws TSLoaderException when an error during loading is encountered.
	 */
	public TimeSeries(TimeSeriesLoader source) throws TSLoaderException {
		//TODO ascending order testing?
		TimeSeriesPoint input;
		while (null != (input = source.readPoint())) {
			points.add(input);
		}
	}
	
	/**
	 * @return <code>true</code> when there is no point in this time series, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return points.isEmpty();
	}
	
	public TimeSeriesIterator iterator() {
		return new TimeSeriesIteratorImpl(points.listIterator());
	}
	
}
