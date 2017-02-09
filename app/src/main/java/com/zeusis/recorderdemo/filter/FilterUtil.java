package com.zeusis.recorderdemo.filter;

import com.arcsoft.filter.ArcFilterGlobalDefine;

public class FilterUtil {
    

    public static  int indexFilterEffectId(int id) {
        return ARCFILTER_EFFECT_ID[id][0];
        }
    
    public static int indexEffectStyleId(int id) {
        return ARCFILTER_EFFECT_ID[id][1];
    }

    public static final int[][] ARCFILTER_EFFECT_ID = {
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_BLACKWHITE2, 0x00000001 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_NEWWARMCOLOR, 0x00000005 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_REVERSELOMO, 0x00000006 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_NATURE, 0x00000007 },
            
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_NONE, 0x00000000 },
            
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_SMALLFRESH, 0x00000002 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_AIBAOCOLOR, 0x00000008 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_NEWCOLORBOOTS, 0x00000004 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_GORGEOUS, 0x00000003 },
           
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_GOLDENTIME, 0x00000009 },
            { ArcFilterGlobalDefine.ARCFILTER_EFFECTID_FILTER_AMARO2, 0x0000000A },

    };
    
}
