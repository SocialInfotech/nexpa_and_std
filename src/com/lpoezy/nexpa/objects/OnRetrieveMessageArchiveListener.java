package com.lpoezy.nexpa.objects;

import java.util.List;

/**
 * Created by HP PAVILION on 3/29/2016.
 */
public interface OnRetrieveMessageArchiveListener {

    public void onRetrieveMessageArchive(final List<MessageResultElement> msgs, final int first, final int last, final int count);
}
