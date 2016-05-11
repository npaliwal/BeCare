package com.github.pocmo.sensordashboard_old.events;

import com.github.pocmo.sensordashboard_old.data.TagData;

public class TagAddedEvent {
    private TagData mTagData;

    public TagAddedEvent(TagData pTagData) {
        mTagData = pTagData;
    }

    public TagData getTag() {
        return mTagData;
    }
}
