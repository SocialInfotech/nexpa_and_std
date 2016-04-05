package com.lpoezy.nexpa.objects;

import com.novoda.sax.RootElement;
import com.novoda.sexp.RootTag;
import com.novoda.sexp.Streamer;
import com.novoda.sexp.finder.ElementFinder;

//<item id='ee0bfce2-46ac-4914-8e47-0e55f785a6089'><broadcast xmlns='pubsub:nexpa:broadcast'>fggggffhhffhhh</broadcast></item>
public class PubSubBroadcastStreamer implements Streamer {

    private final ElementFinder<String> elementFinder;
    private final String elementTag;

    public PubSubBroadcastStreamer(ElementFinder<String> elementFinder, String elementTag) {
        this.elementFinder = elementFinder;
        this.elementTag = elementTag;
    }

    @Override
    public RootTag getRootTag() {
        return RootTag.create("item");
    }

    @Override
    public void stream(RootElement rootElement) {
        elementFinder.find(rootElement, elementTag);
    }

    @Override
    public String getStreamResult() {
        return elementFinder.getResultOrThrow();
    }
}
