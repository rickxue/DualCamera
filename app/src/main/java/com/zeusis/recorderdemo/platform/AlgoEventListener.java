package com.zeusis.recorderdemo.platform;

import android.hardware.Camera;

public interface AlgoEventListener extends Camera.ZsAlgoEvtListener {
    public static final int EVENT_TYPE_NONE = 0;
    public static final int EVENT_TYPE_AUTO_SCENE_DETECT = 1;

    public static final int EVENT_VALUE_AUTO_SCENE_NORMAL = 0;
    public static final int EVENT_VALUE_AUTO_SCENE_NIGHT = 1;
    public static final int EVENT_VALUE_AUTO_SCENE_HDR = 2;
    public static final int EVENT_VALUE_AUTO_SCENE_MOVING = 3;
}
