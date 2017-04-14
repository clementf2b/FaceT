package fyp.hkust.facet.util;

public interface CallbackItemTouch {

    /**
     * Called when an item has been dragged
     *
     * @param oldPosition start position
     * @param newPosition end position
     */
    void itemTouchOnMove(int oldPosition, int newPosition);
}