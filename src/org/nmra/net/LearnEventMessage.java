package org.nmra.net;

/**
 * Learn Event message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class LearnEventMessage extends Message {
    
    public LearnEventMessage(NodeID source, EventID eid) {
        super(source);
        this.eventID = eid;
    }
        
    EventID eventID;
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleLearnEvent(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        LearnEventMessage p = (LearnEventMessage) o;
        return eventID.equals(p.eventID);
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" LearnEvent "+eventID.toString();     
    }
     
}
