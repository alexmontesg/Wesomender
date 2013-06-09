package es.weso.amg.recommender.util;

import java.util.Comparator;
import java.util.Map;

public class ValueComparatorDesc<K, V extends Comparable<V>> implements
		Comparator<K> {

	private Map<K, V> base;

	public ValueComparatorDesc(Map<K, V> base) {
		this.base = base;
	}

	public int compare(K a, K b) {
		if(base.get(b).compareTo(base.get(a)) == 0) {
			return -1;
		}
		return base.get(b).compareTo(base.get(a));
	}

}
