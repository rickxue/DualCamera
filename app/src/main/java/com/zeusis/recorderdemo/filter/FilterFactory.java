package com.zeusis.recorderdemo.filter;

public class FilterFactory {
    public static <T extends IFilterEngine> T createFilterEngine(Class<T> c) {
        IFilterEngine filterEngine = null;
        try {
            filterEngine = (IFilterEngine) Class.forName(c.getName()).newInstance();
        } catch (Exception e) {

        }
        return (T) filterEngine;
    }
    
    public static IFilterEngine getFilterEngine(){
        return createFilterEngine(ArcSoftFilterEngine.class);
    }
}
