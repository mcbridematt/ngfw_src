/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module.  An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.untangle.node.spam;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.untangle.node.mail.papi.AddressKind;
import com.untangle.node.mail.papi.MessageInfo;
import org.hibernate.annotations.Type;

/**
 * Log for SMTP Spam events.
 *
 * @author <a href="mailto:amread@untangle.com">Aaron Read</a>
 * @version 1.0
 */
@Entity
@org.hibernate.annotations.Entity(mutable=false)
@Table(name="n_spam_evt_smtp", schema="events")
public class SpamSmtpEvent extends SpamEvent
{
    private MessageInfo messageInfo;
    private float score;
    private boolean isSpam;
    private SmtpSpamMessageAction action;
    private String vendorName;

    // constructors -----------------------------------------------------------

    public SpamSmtpEvent() { }

    public SpamSmtpEvent(MessageInfo messageInfo, float score, boolean isSpam,
                         SmtpSpamMessageAction action, String vendorName)
    {
        this.messageInfo = messageInfo;
        this.score = score;
        this.isSpam = isSpam;
        this.action = action;
        this.vendorName = vendorName;
    }

    // SpamEvent methods ------------------------------------------------------

    @Transient
    public String getType()
    {
        return "SMTP";
    }

    @Transient
    public int getActionType()
    {
        char type = (null == action) ? SmtpSpamMessageAction.PASS_KEY : action.getKey();
        if (SmtpSpamMessageAction.PASS_KEY == type) {
            return PASSED;
        } else if (SmtpSpamMessageAction.MARK_KEY == type) {
            return MARKED;
        } else if (SmtpSpamMessageAction.BLOCK_KEY == type) {
            return BLOCKED;
        } else if (SmtpSpamMessageAction.QUARANTINE_KEY == type) {
            return QUARANTINED;
        } else if (SmtpSpamMessageAction.SAFELIST_KEY == type) {
            return SAFELISTED;
        } else if (SmtpSpamMessageAction.OVERSIZE_KEY == type) {
            return OVERSIZED;
        } else { // unknown
            return -1;
        }
    }

    @Transient
    public String getActionName()
    {
        if (null == action) {
            return SmtpSpamMessageAction.PASS.getName();
        } else {
            return action.getName();
        }
    }

    // Better sender/receiver info available for smtp
    @Transient
    public String getSender()
    {
        String sender = get(AddressKind.ENVELOPE_FROM);
        if (sender.equals(""))
            // Just go back to the FROM header (if any).
            return super.getSender();
        else
            return sender;
    }

    @Transient
    public String getReceiver()
    {
        String receiver = get(AddressKind.ENVELOPE_TO);

        // This next should never happen, but just in case...
        if (receiver.equals(""))
            // Just go back to the TO header (if any).
            return super.getReceiver();
        else
            return receiver;
    }


    // accessors --------------------------------------------------------------

    /**
     * Associate e-mail message info with event.
     *
     * @return e-mail message info.
     */
    @ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="msg_id")
    public MessageInfo getMessageInfo()
    {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo messageInfo)
    {
        this.messageInfo = messageInfo;
    }

    /**
     * Spam scan score.
     *
     * @return the spam score
     */
    @Column(nullable=false)
    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    /**
     * Was it declared spam?
     *
     * @return true if the message is declared to be Spam
     */
    @Column(name="is_spam", nullable=false)
    public boolean isSpam()
    {
        return isSpam;
    }

    public void setSpam(boolean isSpam)
    {
        this.isSpam = isSpam;
    }

    /**
     * The action taken
     *
     * @return action.
     */
    @Type(type="com.untangle.node.spam.SmtpSpamMessageActionUserType")
    public SmtpSpamMessageAction getAction()
    {
        return action;
    }

    public void setAction(SmtpSpamMessageAction action)
    {
        this.action = action;
    }

    /**
     * Spam scanner vendor.
     *
     * @return the vendor
     */
    @Column(name="vendor_name")
    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }
}
