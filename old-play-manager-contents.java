/*private void setCurrentItem(int itemIndex, long positionMs, boolean playWhenReady) {
    maybeSetCurrentItemAndNotify(itemIndex);
    if (currentPlayer == castPlayer && castPlayer.getCurrentTimeline().isEmpty()) {
        MediaQueueItem[] items = new MediaQueueItem[mediaQueue.size()];
        for (int i = 0; i < items.length; i++) {
            MusicFile item = mediaQueue.get(i);
            items[i] = mediaItemConverter.toMediaQueueItem(musicToMedia(item));
        }
        castPlayer.loadItems(items, itemIndex, positionMs, Player.REPEAT_MODE_OFF);
    } else {
        currentPlayer.seekTo(itemIndex, positionMs);
        currentPlayer.setPlayWhenReady(playWhenReady);
    }
}

private void maybeSetCurrentItemAndNotify(int currentItemIndex) {
    if (this.currentItemIndex != currentItemIndex) {
        Log.d(TAG, "Updating queue item");
        int oldIndex = this.currentItemIndex;
        this.currentItemIndex = currentItemIndex;
        queueListener.onQueuePositionChanged(oldIndex, currentItemIndex);
        audioListener.onTrackMetadataChange(getCurrentMusic());
    }
}*/


/*    public boolean removeItem(MusicFile item) {
        int itemIndex = mediaQueue.indexOf(item);
        if (itemIndex == -1) {
            return false;
        }
        concatenatingMediaSource.removeMediaSource(itemIndex);
        if (currentPlayer == castPlayer) {
            if (castPlayer.getPlaybackState() != Player.STATE_IDLE) {
                Timeline castTimeline = castPlayer.getCurrentTimeline();
                if (castTimeline.getPeriodCount() <= itemIndex) {
                    return false;
                }
                castPlayer.removeItem((int) castTimeline.getPeriod(itemIndex, new Period()).id);
            }
        }
        mediaQueue.remove(itemIndex);
        if (itemIndex == currentItemIndex && itemIndex == mediaQueue.size()) {
            //maybeSetCurrentItemAndNotify(C.INDEX_UNSET);
        } else if (itemIndex < currentItemIndex) {
            //maybeSetCurrentItemAndNotify(currentItemIndex - 1);
        }
        return true;
    }*/

/*    public boolean moveItem(MusicFile item, int toIndex) {
        int fromIndex = mediaQueue.indexOf(item);
        if (fromIndex == -1) {
            return false;
        }
        // Player update.
        concatenatingMediaSource.moveMediaSource(fromIndex, toIndex);
        if (currentPlayer == castPlayer && castPlayer.getPlaybackState() != Player.STATE_IDLE) {
            Timeline castTimeline = castPlayer.getCurrentTimeline();
            int periodCount = castTimeline.getPeriodCount();
            if (periodCount <= fromIndex || periodCount <= toIndex) {
                return false;
            }
            int elementId = (int) castTimeline.getPeriod(fromIndex, new Period()).id;
            castPlayer.moveItem(elementId, toIndex);
        }

        mediaQueue.add(toIndex, mediaQueue.remove(fromIndex));

        // Index update.
        if (fromIndex == currentItemIndex) {
            //maybeSetCurrentItemAndNotify(toIndex);
        } else if (fromIndex < currentItemIndex && toIndex >= currentItemIndex) {
            //maybeSetCurrentItemAndNotify(currentItemIndex - 1);
        } else if (fromIndex > currentItemIndex && toIndex <= currentItemIndex) {
            //maybeSetCurrentItemAndNotify(currentItemIndex + 1);
        }

        return true;
    }*/


/*    public void addItem(MusicFile item) {
        if(!mediaLookup.containsKey(item.LocalFilePath)){
            mediaLookup.put(item.LocalFilePath,item);
            mediaQueue.add(item);
            concatenatingMediaSource.addMediaSource(buildMediaSource(item));
            if (currentPlayer == castPlayer) {
                castPlayer.addItems(mediaItemConverter.toMediaQueueItem(musicToMedia(item)));
            }
        }
    }*/
