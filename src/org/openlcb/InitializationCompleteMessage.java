package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Initialization Complete message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class InitializationCompleteMessage extends Message {
    
    public InitializationCompleteMessage(NodeID source) {
        super(source);
    }
        
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleInitializationComplete(this, sender);
     }
    public String toString() {
        return super.toString()
                +" Initialization Complete";    
    }

    public int getMTI() { return MTI_INITIALIZATION_COMPLETE; }
}
