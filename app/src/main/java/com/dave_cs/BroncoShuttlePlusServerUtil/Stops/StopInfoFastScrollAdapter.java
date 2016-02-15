package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.content.Context;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by David on 2/6/2016.
 */
public class StopInfoFastScrollAdapter extends StopInfoAdapter implements SectionIndexer {

    HashMap<String, Integer> azIndexer;
    String[] sections;

    public StopInfoFastScrollAdapter(Context context, List<StopInfo> stops) {
        super(context, stops);
        initialize();
    }

    public void initialize()
    {
        azIndexer = new HashMap<>();

        int size = super.getCount();

        for(int i = 0; i < size; i++)
        {
            azIndexer.put(super.getItem(i).getName().substring(0,1),i);
        }

        Set<String> keys = azIndexer.keySet();

        Iterator<String> it = keys.iterator();
        ArrayList<String> keyList = new ArrayList<>();

        while (it.hasNext()) {
            String key = it.next();
            keyList.add(key);
        }
        Collections.sort(keyList);//sort the key list
        sections = new String[keyList.size()]; // simple conversion to array
        keyList.toArray(sections);
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        String letter = sections[sectionIndex];
        return azIndexer.get(letter);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    public List<StopInfo> getList() {
        return super.list;
    }
}
