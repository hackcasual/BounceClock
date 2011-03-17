package net.hackcasual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Digit {

	
	static final List<Set<PointI>> SEGMENT_POINTS = new ArrayList<Set<PointI>>();
	
	static {
		Set<PointI> zero = new HashSet<PointI>();
		zero.add(new PointI(0,0));
		zero.add(new PointI(1,0));
		zero.add(new PointI(2,0));
		zero.add(new PointI(3,0));
		SEGMENT_POINTS.add(zero);
		
		Set<PointI> one = new HashSet<PointI>();
		one.add(new PointI(0,0));
		one.add(new PointI(0,1));
		one.add(new PointI(0,2));
		one.add(new PointI(0,3));
		SEGMENT_POINTS.add(one);

		Set<PointI> two = new HashSet<PointI>();
		two.add(new PointI(3,0));
		two.add(new PointI(3,1));
		two.add(new PointI(3,2));
		two.add(new PointI(3,3));
		SEGMENT_POINTS.add(two);
		
		Set<PointI> three = new HashSet<PointI>();
		three.add(new PointI(0,3));
		three.add(new PointI(1,3));
		three.add(new PointI(2,3));
		three.add(new PointI(3,3));
		SEGMENT_POINTS.add(three);

		Set<PointI> four = new HashSet<PointI>();
		four.add(new PointI(0,3));
		four.add(new PointI(0,4));
		four.add(new PointI(0,5));
		four.add(new PointI(0,6));
		SEGMENT_POINTS.add(four);

		Set<PointI> five = new HashSet<PointI>();
		five.add(new PointI(3,3));
		five.add(new PointI(3,4));
		five.add(new PointI(3,5));
		five.add(new PointI(3,6));
		SEGMENT_POINTS.add(five);
				
		Set<PointI> six = new HashSet<PointI>();
		six.add(new PointI(0,6));
		six.add(new PointI(1,6));
		six.add(new PointI(2,6));
		six.add(new PointI(3,6));		
		SEGMENT_POINTS.add(six);
	}
	
	final static Map<Integer, Set<Integer>> NUM_TO_SEGMENTS = new HashMap<Integer, Set<Integer>>();
	
	static {
		NUM_TO_SEGMENTS.put(0, new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,2,4,5,6})));
		NUM_TO_SEGMENTS.put(1, new HashSet<Integer>(Arrays.asList(new Integer[]{2,5})));
		NUM_TO_SEGMENTS.put(2, new HashSet<Integer>(Arrays.asList(new Integer[]{0,2,3,4,6})));		
		NUM_TO_SEGMENTS.put(3, new HashSet<Integer>(Arrays.asList(new Integer[]{0,2,3,5,6})));
		NUM_TO_SEGMENTS.put(4, new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3,5})));
		NUM_TO_SEGMENTS.put(5, new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,3,5,6})));
		NUM_TO_SEGMENTS.put(6, new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,3,4,5,6})));
		NUM_TO_SEGMENTS.put(7, new HashSet<Integer>(Arrays.asList(new Integer[]{0,2,5})));
		NUM_TO_SEGMENTS.put(8, new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,2,3,4,5,6})));
		NUM_TO_SEGMENTS.put(9, new HashSet<Integer>(Arrays.asList(new Integer[]{0,1,2,3,5,6})));		
	}
	
	// Cache the points so we're not newing on the hot loop, reduce GC from 6k obj per 10 seconds to 700
	static Map<Integer, Set<PointI>> pointSetCache = new HashMap<Integer, Set<PointI>>();
	
	public static Set<PointI> getPointsForNum(int n) {
		
		if (pointSetCache.containsKey(n))
			return pointSetCache.get(n);
		
		Set<PointI> points = new HashSet<PointI>();
		
		for (Integer seg: NUM_TO_SEGMENTS.get(n)) {
			points.addAll(SEGMENT_POINTS.get(seg));
		}
		
		pointSetCache.put(n, points);
		
		return points; 
	}
}
