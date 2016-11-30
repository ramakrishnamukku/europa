/*
  $Id: $
  @file ObjectComparator.java
  @brief Contains the ObjectComparator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectComparator
{
    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private static final Map<Object, Long> IDS = new WeakHashMap<>();
    private static CompareTo COMPARE_TO = new CompareTo();
    public static int compareTo(Object a, Object b) {
        return COMPARE_TO.compare(a, b);
    }
    // Implemented as class so we can override identityHashCode() for testing
    // purposes:
    static class CompareTo implements Comparator<Object> {
        protected int identityHashCode(Object obj) {
            return System.identityHashCode(obj);
        }
        @Override
        public int compare(Object a, Object b) {
            if ( a instanceof Comparable ) {
                return ((Comparable)a).compareTo(b);
            } else if ( b instanceof Comparable ) {
                return -((Comparable)b).compareTo(a);
            }
            if ( Objects.deepEquals(a, b) ) return 0;
            int codeA = identityHashCode(a);
            int codeB = identityHashCode(b);
            if ( codeA != codeB ) {
                return codeA < codeB ? -1 : 1;
            }

            synchronized ( IDS ) {
                if ( ! IDS.containsKey(a) ) {
                    IDS.put(a, NEXT_ID.getAndIncrement());
                }
                if ( ! IDS.containsKey(b) ) {
                    IDS.put(b, NEXT_ID.getAndIncrement());
                }
            }
            // NOT synchronized, since we have a reference to a and b on
            // the java stack and the only way a and b could be changed
            // in the map is if a or b got garbage collected.
            int result = IDS.get(a).compareTo(IDS.get(b));
            // This would only happen if we NEXT_ID rolled over and two
            // objects got assigned the same id. Extremely rare!
            if ( 0 == result ) throw new AssertionError();
            return result;
        }
    }
}
