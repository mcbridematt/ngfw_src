/*
 * $Id$
 */
package com.untangle.app.directory_connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.untangle.uvm.util.Pair;
import com.untangle.app.directory_connector.GroupEntry;
import com.untangle.app.directory_connector.ActiveDirectoryServer;
import com.untangle.app.directory_connector.UserEntry;


/**
 * Abstract base class for "Adapters" which call LDAP
 * to perform various operations.
 *
 */
abstract class LdapAdapter
{

    private final Logger logger = Logger.getLogger(LdapAdapter.class);

    /**
     * For subclasses to hold the settings.
     *
     * @return the settings
     */
    protected abstract ActiveDirectoryServer getSettings();

    /**
     * Get the type of object used to describe "people"
     * in this directory (e.g. "user" or "inetOrgPerson").
     */
    protected abstract String[] getUserClassType();

    protected abstract String[] getGroupClassType();

    /**
     * Get the attribute used to hold the mail (e.g. "mail").
     */
    protected abstract String getMailAttributeName();
    
    protected abstract String getPrimaryGroupIDAttribute();

    /**
     * Get the name of the attribute used to hold
     * the full name (i.e. "CN")
     */
    protected abstract String getFullNameAttributeName();

    /**
     * Get the name of the attribute describing the unique id
     * (i.e. "uid" or "sAMAccountName").
     */
    protected abstract String getUIDAttributeName();

    /**
     * Gets search base from repository settings in dependent way.
     */
    protected abstract List<String> getSearchBases();

    protected abstract String getSuperuserDN();

    /**
     * Authenticate the given uid/pwd combination.
     *
     * @param uid the userid
     * @param pwd the password
     *
     * @return true if authentication successful, false otherwise.
     *
     * @exception ServiceUnavailableException if the back-end communication
     *            with the repository is somehow hosed.
     */
    public abstract boolean authenticate( String uid, String pwd )
        throws ServiceUnavailableException;

    /**
     * List all user entries.  Returns an empty list if
     * there are no entries.
     *
     * @return all entries in this repository.
     *
     * @exception ServiceUnavailableException if the back-end communication
     *            with the repository is somehow hosed.
     */
    public List<UserEntry> listAll()
        throws ServiceUnavailableException
    {

        try {
            List<Map<String, String[]>> list = queryAsSuperuser(getSearchBases(),
                                                                getListAllUsersSearchString(),
                                                                getUserEntrySearchControls());

            List<UserEntry> ret = new ArrayList<UserEntry>();

            if(list == null || list.size() == 0) {
                return ret;
            }

            for(Map<String, String[]> map : list) {
                UserEntry entry = toUserEntry(map);
                if(entry != null) {
                    ret.add(entry);
                }
            }

            Collections.sort(ret);
            return ret;
        }catch(NamingException ex) {
            logger.warn("Exception listing entries", ex);
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    /**
     * Get all of the groups that are available for this adapter.
     * 
     * @param fetchMembersOf
     *            Set to true to indicate that the entries should include the
     *            list of groups that the group is a member of.
     * @return
     */
    public abstract List<GroupEntry> listAllGroups( boolean fetchMembersOf )
        throws ServiceUnavailableException;
    
    /**
     * Get all of the groups that a user belongs to.
     * @param user The username to query.
     * @return A List of all of the groups that a user belongs to.
     */
    public abstract List<GroupEntry> listUserGroups( String user ) throws ServiceUnavailableException;
    
    /**
     * Get all of the users that belong to a group.
     * @param group Name of the group to query.
     * @return A list of all of the users that belong to a group.
     */
    public abstract List<UserEntry> listGroupUsers( String group ) throws ServiceUnavailableException;

    /**
     * Tests if the given userid exists in this repository
     *
     * @param uid the userid
     *
     * @return true if it exists
     *
     * @exception ServiceUnavailableException if the back-end communication
     *            with the repository is somehow hosed.
     */
    public boolean containsUser( String uid )
        throws ServiceUnavailableException
    {
        return getEntry(uid) != null;
    }


    /**
     * Get a UserEntry by email address.  If nore than one user share emails,
     * this returns the <b>first</b> user.
     *
     * @param email the email address
     *
     * @return the UserEntry, or null if no entry corresponding to that
     *         email was found
     *
     * @exception ServiceUnavailableException if the back-end communication
     *            with the repository is somehow hosed.
     */
    public UserEntry getEntryByEmail( String email )
        throws ServiceUnavailableException
    {

        StringBuilder sb = new StringBuilder();
        sb.append("(&");
        sb.append(orStrings("objectClass=", getUserClassType()));
        sb.append("(").append(getMailAttributeName()).append("=").append(email).append(")");
        sb.append(")");

        return getEntryWithSearchString(sb.toString());

    }

    /**
     * Get a UserEntry by userid.  If nore than one entry is found (!),
     * this returns the <b>first</b> user.
     *
     * @param uid the username
     * @return the UserEntry, <b>or null if no entry corresponding to that
     *         uid was found</b>
     *
     * @exception ServiceUnavailableException if the back-end communication
     *            with the repository is somehow hosed.
     */
    public UserEntry getEntry( String uid )
        throws ServiceUnavailableException
    {

        StringBuilder sb = new StringBuilder();
        sb.append("(&");
        sb.append(orStrings("objectClass=", getUserClassType()));
        sb.append("(").append(getUIDAttributeName()).append("=").append(uid).append(")");
        sb.append(")");

        return getEntryWithSearchString(sb.toString());
    }

    public static String orStrings( String prefix, String[] values )
    {
        return queryStrings(prefix, values, "|", "(", ")");
    }

    public static String queryStrings( String prefix, String[] values, String operator, String open, String close )
    {
        StringBuilder results = new StringBuilder();
        queryStringsRecursive(prefix, values, operator, open, close, results);
        return results.toString();
    }
    
    /**
     * rbscott is not sure why this is recursive and thinks it should be deprecated for the non-recursive remix joinValues. XXX
     * @param prefix
     * @param values
     * @param operator
     * @param open
     * @param close
     * @param results
     */
    protected static void queryStringsRecursive( String prefix, String[] values, String operator, String open, String close, StringBuilder results )
    {
        if (values.length == 1) {
            results.append(open + prefix + values[0] + close);
        } else {
            results.append(open + operator + open + prefix + values[0] + close);
            String[] valuesDequed = new String[values.length-1];
            System.arraycopy(values, 1, valuesDequed, 0, values.length-1);
            queryStringsRecursive(prefix, valuesDequed, operator, open, close, results);
            results.append(close);
        }
    }

    /**
     * Join a series of criteria into a larger string. For instance to join
     * "objectClass", {user | group} ->
     * (|(objectClass=user)(objectClass=group))
     * 
     * @param operator The operator to link the criteria together, typically | or &
     * @param parameter The name of the field to link together. (eg. objectClass=)
     * @param values Array of values to link together. (eg. ["user","group"] )
     * @return
     */
    public static String joinCriteria( String operator, String parameter, String[] values )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(operator);
        for ( String value : values ) {
            sb.append("(");
            sb.append(parameter);
            sb.append(value);
            sb.append(")");
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    /**
     * Join a series of criteria into a larger string. For instance to join
     * "objectClass=user" or "objectClass=group" ->
     * (|(objectClass=user)(objectClass=group))
     * 
     * @param operator The operator to link the criteria together, typically | or &
     * @param criteria An array of search criteria, eg objectClass=group
     * @return
     */
    public static String joinCriteria( String operator, String[] criteria )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(operator);
        for ( String component : criteria ) {
            sb.append("(");
            sb.append(component);
            sb.append(")");
        }
        sb.append(")");
        
        return sb.toString();
    }

    /**
     * Tests the settings via creating a superuser context.
     *
     * @exception CommunicationException if the server could not be found
     * @exception AuthenticationException if the server did not like
     *            our superuser credentials
     * @exception NamingException catch-all for "bummer, something else is wrong".
     */
    public void test()
        throws CommunicationException, AuthenticationException, NamingException
    {

        try {
            closeContext(createSuperuserContext());
        }
        catch(NamingException ex) {
            throw ex;
        }

    }

    //========================================
    // Superuser Context Pool Methods
    //========================================


    /**
     * Access for the (maybe someday) pool of
     * superuser connections
     *
     *
     * @return null if no connection can be established.
     */
    protected final DirContext checkoutSuperuserContext()
        throws Exception
    {

        ActiveDirectoryServer settings = getSettings();

        try {
            return createSuperuserContext();
        }
        catch(AuthenticationException ex) {
            logger.warn("Unable to create superuser context with settings: " +
                        "Host: \"" + settings.getLDAPHost() + "\", " +
                        "Port: \"" + settings.getLDAPPort() + "\", " +
                        "Superuser DN: \"" + getSuperuserDN() + "\", " +
                        "Pass: " + (settings.getSuperuserPass()==null?"<null>":"<not null>") +
                        " ; Error is: " + ex.toString());
            throw ex;
        }
        catch(CommunicationException ex) {
            logger.warn("Unable to create superuser context with settings: " +
                        "Host: \"" + settings.getLDAPHost() + "\", " +
                        "Port: \"" + settings.getLDAPPort() + "\", " +
                        "Superuser DN: \"" + getSuperuserDN() + "\", " +
                        "Pass: " + (settings.getSuperuserPass()==null?"<null>":"<not null>") +
                        " ; Error is: " + ex.toString());

            Throwable cause = null; 
            Throwable result = ex;

            while(null != (cause = result.getCause())  && (result != cause) ) {
                result = cause;
            }   

            throw new CommunicationException( result.getMessage() + ": " + ex.getMessage() );
        }
        catch(Exception ex) {
            logger.error("Unable to create superuser context with settings: " +
                         "Host: \"" + settings.getLDAPHost() + "\", " +
                         "Port: \"" + settings.getLDAPPort() + "\", " +
                         "Superuser DN: \"" + getSuperuserDN() + "\", " +
                         "Pass: " + (settings.getSuperuserPass()==null?"<null>":"<not null>") +
                         " ; Error is: " + ex);
            throw ex;
        }
    }

    /**
     * Return the previously checked-out superuser context
     *
     * @param ctx the context
     * @param mayBeBad if true, the context should be considered
     *        suspect.
     */
    protected final void returnSuperuserContext(DirContext ctx, boolean mayBeBad)
    {
        closeContext(ctx);
    }

    //========================================
    // Other Methods Subclasses may use
    //========================================

    /**
     * Convienence methods to convert the communication and authentication excpetions
     * to the more generic "ServiceUnavailableException".
     */
    protected final NamingException convertToServiceUnavailableException(NamingException ex)
    {
        if(ex instanceof CommunicationException) {
            return new ServiceUnavailableException("Communication Failure: " + ex.toString());
        }
        if(ex instanceof AuthenticationException) {
            return new ServiceUnavailableException("Authentication Failure: " + ex.toString());
        }
        return ex;
    }

    /**
     * Convienence method which creates SearchControls with the given
     * attributes to be returned and subtree scope
     */
    protected SearchControls createSimpleSearchControls(String...filter)
    {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(filter);
        return searchCtls;
    }

    /**
     * Name says it all.  Will never throw an
     * exception
     */
    protected final void closeContext(DirContext ctx) {
        try {
            ctx.close();
        }
        catch(Exception ignore) {}
    }

    /**
     * Create a context based on the given parameters
     *
     * @param host the host
     * @param port the port
     * @param dn the DistinguishedName
     * @param pass the password
     */
    protected final DirContext createContext(String host, int port, boolean secure, String dn, String pass)
        throws NamingException
    {
        Hashtable<String, String> ldapEnv = new Hashtable<String, String>(5);
        if( secure ){
            /**
             * Override certificate authentication to allow any certificate.
             * Yes, to be 100% security minded we should only allow specified certificates.
            */
            TrustManager[] trustAllCerts = new TrustManager[] { 
                new X509TrustManager() {     
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                        return new X509Certificate[0];
                    } 
                    public void checkClientTrusted( 
                        java.security.cert.X509Certificate[] certs, String authType) {
                    } 
                    public void checkServerTrusted( 
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }    
            }; 
            javax.net.ssl.SSLContext sslCtx = null;
            try{
                sslCtx = javax.net.ssl.SSLContext.getInstance("SSL");
                sslCtx.init(null, trustAllCerts, null);
            }catch( Exception e ){
                 e.printStackTrace();
            }
            ldapEnv.put( "java.naming.ldap.factory.socket", LdapAdapterSocketFactory.class.getName() );
            LdapAdapterSocketFactory.set( sslCtx.getSocketFactory() );
            ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL,  "ldap" + ( secure ? "s" : "") + "://" + host + ":" + Integer.toString(port));
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        ldapEnv.put(Context.SECURITY_PRINCIPAL, dn);
        ldapEnv.put(Context.SECURITY_CREDENTIALS, pass);
        return new InitialDirContext(ldapEnv);
    }

    /**
     * Create a DirContext based on the current superuser
     * parameters
     *
     *
     * @return a DirContext as the superuser.
     */
    protected final DirContext createSuperuserContext()
        throws CommunicationException, AuthenticationException, NamingException
    {
        ActiveDirectoryServer settings = getSettings();
        return createContext(settings.getLDAPHost(),
                             settings.getLDAPPort(),
                             settings.getLDAPSecure(),
                             getSuperuserDN(),
                             settings.getSuperuserPass());
    }

    /**
     * Intended to be overidden by Active Directory
     */
    protected String getListAllUsersSearchString()
    {
        return orStrings("objectClass=", getUserClassType());
    }

    protected String getListAllGroupsSearchString()
    {
        return orStrings("objectClass=", getGroupClassType());
    }

    /**
     * @param gid the group id in question
     * @return true if it should be skipped, false if it is included in this directory
     */
    protected boolean filterGID(int gid)
    {
        return gid < 20000 || gid > 1000000;
    }

    /**
     * Perform the given query as a superuser.  This
     * method will try "twice" to make sure it is not
     * trying to reuse a dead connection.
     *
     * @param searchBase the base of the search
     * @param searchFilter the query string
     * @param ctls the search controls
     *
     * @return all returned data
     */
    protected final List<Map<String, String[]>> queryAsSuperuser(List<String> searchBases, String searchFilter, SearchControls ctls)
        throws NamingException, ServiceUnavailableException
    {
        List<Map<String, String[]>> results = new ArrayList<Map<String, String[]>>();

        for( String searchBase : searchBases){
            results.addAll(queryAsSuperuserImpl(searchBase, searchFilter, ctls, true));
        }

        return results;
    }

    /**
     * Perform the given query (only attempts once as
     * the context was passed-in), and return the specified attributes.
     *
     * @param searchBase the base of the search
     * @param searchFilter the query string
     * @param ctls the search controls specifying which attributes to return
     * @param ctx the context to use for the query.
     *
     * @return all returned data
     */
    protected final List<Map<String, String[]>> query(String searchBase, String searchFilter, SearchControls ctls, DirContext ctx)
        throws ServiceUnavailableException, NamingException
    {

        try {
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter, ctls);
            List<Map<String, String[]>> ret = new ArrayList<Map<String, String[]>>();
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();
                
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    Map<String, String[]> map = new HashMap<String, String[]>();
                    for (NamingEnumeration<?> ae = attrs.getAll();ae.hasMore();) {
                        Attribute attr = (Attribute)ae.next();
                        String attrName = attr.getID();
                        ArrayList<String> values = new ArrayList<String>();
                        NamingEnumeration<?> e = attr.getAll();
                        while(e.hasMore()) {
                            values.add(e.next().toString());
                        }
                        map.put(attrName, values.toArray(new String[values.size()]));
                    }
                    
                    try {
                        map.put( "dn=", new String[] { sr.getNameInNamespace()});
                    } catch ( UnsupportedOperationException e ) {
                        logger.warn( "Unable to determine fully qualified DN for a result", e);
                    }
                    ret.add(map);
                }
            }
            return ret;
        }
        catch(NamingException ex) {
            throw convertToServiceUnavailableException(ex);
        }
    }

    /**
     * ...name says it all.  Note that this tries "twice" in case
     * the superuser connection was dead
     */
    protected final void createSubcontextAsSuperuser(String dn, BasicAttributes attribs)
        throws NamingException, NameAlreadyBoundException, ServiceUnavailableException
    {
        createSubcontextAsSuperuserImpl(dn, attribs, true);
    }

    //========================================
    // Helper Methods
    //========================================

    /**
     * Returns null if not found.  If more than one found (!),
     * first is returned.
     */
    private UserEntry getEntryWithSearchString(String searchStr)
        throws ServiceUnavailableException
    {

        try {

            List<Map<String, String[]>> list =
                queryAsSuperuser(getSearchBases(),
                                 searchStr,
                                 getUserEntrySearchControls());


            if(list == null || list.size() == 0) {
                return null;
            }

            return toUserEntry(list.get(0));
        }
        catch(NamingException ex) {
            throw new ServiceUnavailableException(ex.toString());
        }
    }

    /**
     * Perform the superuser query, repeating again in case a dead
     * connection was used.
     */
    private List<Map<String, String[]>> queryAsSuperuserImpl( String searchBase, String searchFilter, SearchControls ctls, boolean tryAgain)
        throws NamingException, ServiceUnavailableException
    {
        DirContext ctx = null;
        try {
            ctx = checkoutSuperuserContext();
        } catch (Exception e) {
            throw new ServiceUnavailableException(e.getMessage());
        }
        if( ctx == null ) {
            throw new ServiceUnavailableException("Unable to obtain context");
        }

        try {
            List<Map<String, String[]>> ret = query(searchBase, searchFilter, ctls, ctx);
            returnSuperuserContext(ctx, false);
            return ret;
        }
        catch(NamingException ex) {
            returnSuperuserContext(ctx, true);
            if(tryAgain) {
                return queryAsSuperuserImpl(searchBase, searchFilter, ctls, false);
            }
            else {
                throw convertToServiceUnavailableException(ex);
            }
        }
    }

    /**
     * Used to repeat the creating subcontext thing twice
     * should a connection be stale
     */
    private void createSubcontextAsSuperuserImpl(String dn, BasicAttributes attribs, boolean tryAgain)
        throws NameAlreadyBoundException, ServiceUnavailableException, NamingException
    {
        DirContext ctx = null;
        try {
            ctx = checkoutSuperuserContext();
        } catch (Exception e) {
            throw new ServiceUnavailableException(e.getMessage());
        }

        if(ctx == null) {
            throw new ServiceUnavailableException("Unable to obtain context");
        }


        try {
            ctx.createSubcontext(dn, attribs);
            returnSuperuserContext(ctx, false);
            return;
        }
        catch(NameAlreadyBoundException nabe) {
            returnSuperuserContext(ctx, false);
            throw nabe;
        }
        catch(NamingException ex) {
            returnSuperuserContext(ctx, true);
            if(tryAgain) {
                createSubcontextAsSuperuserImpl(dn, attribs, false);
            }
            else {
                throw convertToServiceUnavailableException(ex);
            }
        }
    }

    /**
     * Helper to convert (based on our "standard" returned controls)
     * the USerEntry.
     */
    protected UserEntry toUserEntry(Map<String, String[]> map)
    {
        Pair<String, String> parsedName = parseFullName(getFirstEntryOrNull(map.get(getFullNameAttributeName())));

        return new UserEntry(getFirstEntryOrNull(map.get(getUIDAttributeName())),
                             parsedName.a,
                             parsedName.b,
                             getFirstEntryOrNull(map.get(getMailAttributeName())),
                             getFirstEntryOrNull(map.get(getPrimaryGroupIDAttribute())),
                             getFirstEntryOrNull(map.get("dn=")));
    }

    /**
     * Helper to convert (based on our "standard" returned controls)
     * the GroupEntry.
     */
    protected GroupEntry toGroupEntry(Map<String, String[]> map)
    {
        String[] memberOf = map.get("memberOf");
        Set<String> memberOfSet = null;
        if ( memberOf != null ) {
            memberOfSet = new HashSet<String>(memberOf.length);
            for ( String groupName : memberOf ) {
                memberOfSet.add(groupName);
            }
        }
        GroupEntry entry= new GroupEntry(
                                         getFirstEntryOrNull(map.get(getCNName())),
                                         0, //TODO fix this later
                                         getFirstEntryOrNull(map.get(getGroupName())),
                                         getFirstEntryOrNull(map.get(getGroupTypeName())),
                                         getFirstEntryOrNull(map.get(getGroupDescriptionName())),
                                         getFirstEntryOrNull(map.get("dn=")),
                                         memberOfSet,
                                         getSettings().getDomain());
        
        entry.setPrimaryGroupToken(getFirstEntryOrNull(map.get("primaryGroupToken")));
        return entry;
    }

    protected String getCNName()
    {
        return "cn";
    }

    protected String getGroupName()
    {
        return "sAMAccountName";
    }

    protected String getGroupTypeName()
    {
        return "samaccounttype";
    }

    protected String getGroupDescriptionName()
    {
        return "description";
    }

    protected String getGroupMembersName()
    {
        return "members";
    }
    
    /**
     * Gets the first entry in the String[], or null
     */
    protected String getFirstEntryOrNull(String[] str)
    {
        return (str==null || str.length ==0)?
            null:str[0];
    }

    /**
     * Helper to create the search controls used when fetching a UserEntry
     */
    protected SearchControls getUserEntrySearchControls()
    {
        return createSimpleSearchControls( getUIDAttributeName(),
                                           getMailAttributeName(),
                                           getPrimaryGroupIDAttribute(),
                                           getFullNameAttributeName());
    }

    /**
     * Helper to create the search controls used when fetching a GroupEntry
     */
    protected SearchControls getGroupEntrySearchControls(boolean fetchMembersOf)
    {
        if ( fetchMembersOf ) {
            return createSimpleSearchControls("cn",
                                              "description",
                                              getGroupName(),
                                              "sAMAccountType",
                                              "groupType",
                                              "memberOf",
                                              "primaryGroupToken");
        }
        
        return createSimpleSearchControls("cn",
                                          "description",
                                          getGroupName(),
                                          "sAMAccountType",
                                          "groupType",
                                          "primaryGroupToken");
    }
    
    /**
     * If input is null, members of pair are null.  If no space,
     * then only firrst name is returned (e.g. "Bono" or "Sting")
     * and last name is "".  If entire String is "", then
     * the pair is "", ""
     */
    private final Pair<String, String> parseFullName(String str)
    {
        if(str == null) {
            return new Pair<String, String>(null, null);
        }
        str = str.trim();

        int index = str.indexOf(' ');

        if(index == -1) {
            return new Pair<String, String>(str, "");
        }
        return new Pair<String, String>(str.substring(0, index).trim(), str.substring(index).trim());
    }

    /**
     * Helper function
     */
    protected String domainComponents(String dom)
    {
        if (dom.toUpperCase().startsWith("DC="))
            return dom;
        while (dom.endsWith("."))
            dom = dom.substring(0, dom.length() - 1);
        return "DC=" + dom.replace(".", ",DC=");
    }

}


