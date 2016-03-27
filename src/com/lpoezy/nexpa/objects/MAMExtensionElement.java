package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.packet.ExtensionElement;

/**
 * Created by ldonios on 3/26/2016.
 */
public class MAMExtensionElement implements ExtensionElement {
    @Override
    public String getNamespace() {
        return "result";
    }

    @Override
    public String getElementName() {
        return "urn:xmpp:mam:0";
    }

    @Override
    public CharSequence toXML() {
        return "<message to='kato@198.154.106.139/Smack'><result xmlns='urn:xmpp:mam:0'><thread>2ecbe32c-4f8d-4287-a09d-e71a4c9187e7</thread></result></message>";
    }



}
