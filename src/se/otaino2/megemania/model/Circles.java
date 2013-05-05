package se.otaino2.megemania.model;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseIntArray;

/**
 * A collection of circles
 * 
 * @author otaino-2
 *
 */
public class Circles {
    
    private List<Circle> circles;
    
    private SparseIntArray typeCount; 
    
    public Circles() {
        this.circles = new ArrayList<Circle>();
        this.typeCount = new SparseIntArray();
    }

    public List<Circle> get() {
        return circles;
    }
    
    public void addCircle(Circle circle) {
        circles.add(circle);
        addType(circle.getPaint().getColor());
    }
    
    public void removeCircle(Circle circle) {
        circles.remove(circle);
        removeType(circle.getPaint().getColor());
    }

    private void addType(int type) {
        int count = typeCount.get(type, 0) + 1;
        typeCount.put(type, count);
    }
    
    private void removeType(int type) {
        int count = typeCount.get(type, 1) - 1;
        typeCount.put(type, count);
    }
    
    public List<Integer> getTypes() {
        List<Integer> types = new ArrayList<Integer>();
        for (int i = 0; i < typeCount.size(); i++) {
            types.add(typeCount.keyAt(i));
        }
        return types;
    }
    
    public int getTypeCount(int type) {
        return typeCount.get(type);
    }
    
    public void clear() {
        this.circles.clear();
        typeCount.clear();
    }
}
