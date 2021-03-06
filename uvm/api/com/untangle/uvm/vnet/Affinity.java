/**
 * $Id$
 */
package com.untangle.uvm.vnet;

/**
 * Represents affinity for a particular side of the pipeline.
 *
 */
public class Affinity
{
    public static final Affinity CLIENT = new Affinity("client",1);
    public static final Affinity MIDDLE = new Affinity("middle",2);
    public static final Affinity SERVER = new Affinity("server",3);

    private String affinity;
    private int numValue;
    
    private Affinity( String affinity, int numValue )
    {
        this.affinity = affinity;
        this.numValue = numValue;
    }

    public String toString()
    {
        return affinity;
    }

    public int numValue()
    {
        return numValue;
    }

        
}
