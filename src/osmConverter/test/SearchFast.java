package osmConverter.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SearchFast {
    final int k = 22000;
    List<String> l1 = new ArrayList<String>();
    List<String> l2 = new ArrayList<String>();

    public SearchFast() {
	Random r = new Random(42);
	for (int i = 0; i < k; i++) {
	    l1.add("String " + r.nextInt(k));
	    l2.add("String " + r.nextInt(k));
	}
	compare1();
	compare2();
    }

    public void compare1() {
	long time = System.currentTimeMillis();
	int c = 0;
	for (int i = 0; i < k; i++) {
	    String st1 = l1.get(i);
	    for (int j = 0; j < k; j++) {
		String st2 = l2.get(j);
		if (st1.equals(st2)) {
		    c++;
		}
	    }
	}
	System.out.println("c1: " + c + ", time: "
		+ (System.currentTimeMillis() - time));
    }

    public void compare2() {
	long time = System.currentTimeMillis();
	Map<String, Integer> mapL1 = new HashMap<String, Integer>();
	for (int i = 0; i < k; i++) {
	    String st1 = l1.get(i);
	    Integer old = mapL1.get(st1);
	    int newC = (old == null ? 0 : old.intValue()) + 1;
	    mapL1.put(st1, Integer.valueOf(newC));
	}

	int c = 0;
	for (int j = 0; j < k; j++) {
	    String st2 = l2.get(j);
	    Integer old = mapL1.get(st2);
	    if (old != null) {
		c += old.intValue();
	    }
	}
	System.out.println("c2: " + c + ", time: "
		+ (System.currentTimeMillis() - time));
    }

    public static void main(String[] args) {
	new SearchFast();
    }

}
