/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 * 
 * The contents of this file are subject to the terms of the Common Development 
 * and Distribution License("CDDL") (the "License").  You may not use this file 
 * except in compliance with the License.
 * 
 * You can obtain a copy of the License at 
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.oracleerp;

import static org.identityconnectors.oracleerp.OracleERPUtil.*;

import java.sql.Timestamp;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.junit.Assert;
import org.junit.Test;



/**
 * Attempts to test the {@link OracleERPConnector} with the framework.
 * 
 * @author petr
 * @version 1.0
 * @since 1.0
 */
public class AccountSQLBuilderTests { 
       
    //Schema id
    private static String SCHEMA_PREFIX="APPS.";

    /**
     * Test method
     */
    @Test
    public void testCreateUserCall() {
        final Set<Attribute> attrs = createAllAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, true).build(ObjectClass.ACCOUNT, attrs, null);

        //test sql
        Assert.assertEquals("Invalid SQL",
                        "{ call APPS.fnd_user_pkg.CreateUser ( x_user_name => ?, x_owner => upper(?), "+
                        "x_unencrypted_password => ?, x_start_date => ?, x_end_date => ?, "+
                        "x_last_logon_date => ?, x_description => ?, x_password_date => ?, "+
                        "x_password_accesses_left => ?, x_password_lifespan_accesses => ?, "+
                        "x_password_lifespan_days => ?, x_employee_id => ?, x_email_address => ?, "+
                        "x_fax => ?, x_customer_id => ?, x_supplier_id => ? ) }",
                        asb.getUserCallSQL());
        //session is always null, so 16 params
        Assert.assertEquals("Invalid number of  SQL Params", 16, asb.getUserSQLParams().size());
        Assert.assertFalse("Is update needed", asb.isUpdateNeeded());
    }    
    
    /**
     * Test method
     */
    @Test
    public void testUpdateUserCall() {
        final Set<Attribute> attrs = createAllAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, false).build(ObjectClass.ACCOUNT, attrs, null);

        //test sql
        Assert.assertEquals("Invalid SQL",
                        "{ call APPS.fnd_user_pkg.UpdateUser ( x_user_name => ?, x_owner => upper(?), "+
                        "x_unencrypted_password => ?, x_start_date => ?, x_end_date => ?, "+
                        "x_description => ?, x_password_date => ?, x_password_accesses_left => ?, "+
                        "x_password_lifespan_accesses => ?, x_password_lifespan_days => ?, x_employee_id => ?, "+
                        "x_email_address => ?, x_fax => ?, x_customer_id => ?, x_supplier_id => ? ) }",
                        asb.getUserCallSQL());
        //session is always null, so 15 params
        Assert.assertEquals("Invalid number of  SQL Params", 15, asb.getUserSQLParams().size());
        Assert.assertFalse("Is update needed", asb.isUpdateNeeded());
    }        
    
    /**
     * Test method
     */
    @Test
    public void testCreateUserCreateUserAll() {
        final Set<Attribute> attrs = createAllAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, true).build(ObjectClass.ACCOUNT, attrs, null);
        
        //Old style of creating user
        Assert.assertEquals("Invalid All SQL",
                "{ call APPS.fnd_user_pkg.CreateUser ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }",
                asb.getAllSQL());
        
        //all 17 params
        Assert.assertEquals("Invalid number of  SQL Params", 17, asb.getAllSQLParams().size());
        Assert.assertFalse("Is update needed", asb.isUpdateNeeded());
    }     
    
    /**
     * Test method
     */
    @Test
    public void testUpdateUserCreateUserAll() {
        final Set<Attribute> attrs = createAllAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, false).build(ObjectClass.ACCOUNT, attrs, null);
        
        //Old style of creating user
        Assert.assertEquals("Invalid All SQL",
                "{ call APPS.fnd_user_pkg.UpdateUser ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }",
                asb.getAllSQL());
        
        //all 17 params
        Assert.assertEquals("Invalid number of  SQL Params", 17, asb.getAllSQLParams().size());
        Assert.assertFalse("Is update needed", asb.isUpdateNeeded());
    }     
    
    /**
     * Test method
     */
    @Test
    public void testCreateUserCallNulls() {
        final Set<Attribute> attrs = createNullAccountAttributes();
        final AccountSQLBuilder asb = new AccountSQLBuilder(SCHEMA_PREFIX, true).build(ObjectClass.ACCOUNT, attrs, null);
        //test sql
        Assert.assertEquals("Invalid SQL",
                "{ call APPS.fnd_user_pkg.CreateUser ( x_user_name => ?, x_owner => upper(?), "+
                "x_unencrypted_password => ?, x_start_date => ?, x_end_date => FND_USER_PKG.null_date, "+
                "x_last_logon_date => ?, x_description => FND_USER_PKG.null_char, "+
                "x_password_date => ?, x_password_accesses_left => FND_USER_PKG.null_number, "+
                "x_password_lifespan_accesses => FND_USER_PKG.null_number, "+
                "x_password_lifespan_days => FND_USER_PKG.null_number, x_employee_id => FND_USER_PKG.null_number, "+
                "x_email_address => FND_USER_PKG.null_char, x_fax => FND_USER_PKG.null_char, "+
                "x_customer_id => FND_USER_PKG.null_number, x_supplier_id => FND_USER_PKG.null_number ) }",
                asb.getUserCallSQL());
        //session is always null, so 16 params
        Assert.assertEquals("Invalid number of  SQL Params", 6, asb.getUserSQLParams().size());    
    }
    
    /**
     * Test method
     */
    @Test
    public void testUpdateUserCallNulls() {
        final Set<Attribute> attrs = createNullAccountAttributes();
        final AccountSQLBuilder asb = new AccountSQLBuilder(SCHEMA_PREFIX, false).build(ObjectClass.ACCOUNT, attrs, null);
        //test sql
        Assert.assertEquals("Invalid SQL",
                "{ call APPS.fnd_user_pkg.UpdateUser ( x_user_name => ?, x_owner => upper(?), "+
                "x_unencrypted_password => ?, x_start_date => ?, x_end_date => FND_USER_PKG.null_date, "+
                "x_description => FND_USER_PKG.null_char, "+
                "x_password_date => ?, x_password_accesses_left => FND_USER_PKG.null_number, "+
                "x_password_lifespan_accesses => FND_USER_PKG.null_number, "+
                "x_password_lifespan_days => FND_USER_PKG.null_number, x_employee_id => FND_USER_PKG.null_number, "+
                "x_email_address => FND_USER_PKG.null_char, x_fax => FND_USER_PKG.null_char, "+
                "x_customer_id => FND_USER_PKG.null_number, x_supplier_id => FND_USER_PKG.null_number ) }",
                asb.getUserCallSQL());
        //session is always null, so 16 params
        Assert.assertEquals("Invalid number of  SQL Params", 5, asb.getUserSQLParams().size());    
    }    

    /**
     * Test method
     */
    @Test
    public void testCreateUserCallAllNulls() {
        final Set<Attribute> attrs = createNullAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, true).build(ObjectClass.ACCOUNT, attrs, null);

        //Test Old style of creating user
        Assert.assertEquals("Invalid All SQL",
                "{ call APPS.fnd_user_pkg.CreateUser ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }",
                asb.getAllSQL());       
        //all 17 params
        Assert.assertEquals("Invalid number of  SQL Params", 17, asb.getAllSQLParams().size());
        Assert.assertTrue("Is update needed", asb.isUpdateNeeded()); 
    }
    
    /**
     * Test method
     */
    @Test
    public void tesUpdateUserCallAllNulls() {
        final Set<Attribute> attrs = createNullAccountAttributes();
        final AccountSQLBuilder asb =  new AccountSQLBuilder(SCHEMA_PREFIX, true).build(ObjectClass.ACCOUNT, attrs, null);

        Assert.assertTrue("Is update needed", asb.isUpdateNeeded());

        Assert.assertEquals("Invalid All SQL",
                "{ call APPS.fnd_user_pkg.UpdateUser ( x_user_name => ?, x_owner => upper(?), "+
                "x_end_date => FND_USER_PKG.null_date, "+
                "x_description => FND_USER_PKG.null_char, x_password_accesses_left => FND_USER_PKG.null_number, "+
                "x_password_lifespan_accesses => FND_USER_PKG.null_number, "+
                "x_password_lifespan_days => FND_USER_PKG.null_number, x_employee_id => FND_USER_PKG.null_number, "+
                "x_email_address => FND_USER_PKG.null_char, x_fax => FND_USER_PKG.null_char, "+
                "x_customer_id => FND_USER_PKG.null_number, x_supplier_id => FND_USER_PKG.null_number ) }",
                asb.getUserUpdateNullsSQL());
        
        //the required user name and owner
        Assert.assertEquals("Invalid number of update SQL Params", 2, asb.getUserUpdateNullsParams().size());        
    }    
    
    
    /**
     * 
     * @return the All account attributes
     */
    static public Set<Attribute> createAllAccountAttributes() {
        final Set<Attribute> attrs = createRequiredAccountAttributes();    
        attrs.add(AttributeBuilder.buildPasswordExpired(false));                
        attrs.add(AttributeBuilder.build(START_DATE, (new Timestamp(System.currentTimeMillis()-10*24*3600000)).toString()));
        attrs.add(AttributeBuilder.build(END_DATE, (new Timestamp(System.currentTimeMillis()+10*24*3600000)).toString()));        
        attrs.add(AttributeBuilder.build(RESPS, "Cash Forecasting||Oracle Cash Management||Standard||Test Description||2004-04-12 00:00:00.0||null"));
        attrs.add(AttributeBuilder.build(PWD_ACCESSES_LEFT, 56));
        attrs.add(AttributeBuilder.build(PWD_LIFESPAN_ACCESSES, 5));
        attrs.add(AttributeBuilder.build(PWD_LIFESPAN_DAYS, 5));
        attrs.add(AttributeBuilder.build(EMP_ID, 5));
        attrs.add(AttributeBuilder.build(DESCR, "Test Description"));        
        attrs.add(AttributeBuilder.build(EMAIL, "person@somewhere.com"));
        attrs.add(AttributeBuilder.build(FAX, "555-555-5555"));
        attrs.add(AttributeBuilder.build(CUST_ID, 11223344));
        attrs.add(AttributeBuilder.build(SUPP_ID, 102));
        attrs.add(AttributeBuilder.build(SEC_ATTRS, "TO_PERSON_ID||Oracle Self-Service Web Applications||110"));
        return attrs;
    }
    
    /**
     * @return the All attributes null
     */
    static public Set<Attribute> createNullAccountAttributes() {
        Set<Attribute> attrs = createRequiredAccountAttributes();
        attrs.add(AttributeBuilder.buildPasswordExpired(false));        
        attrs.add(AttributeBuilder.build(START_DATE, new java.sql.Timestamp(System.currentTimeMillis()).toString()));
        attrs.add(AttributeBuilder.build(END_DATE, (String) null));        
        attrs.add(AttributeBuilder.build(RESPS, (String) null));
        attrs.add(AttributeBuilder.build(PWD_ACCESSES_LEFT, (String) null));
        attrs.add(AttributeBuilder.build(PWD_LIFESPAN_ACCESSES, (String) null));
        attrs.add(AttributeBuilder.build(PWD_LIFESPAN_DAYS, (String) null));
        attrs.add(AttributeBuilder.build(EMP_ID, (String) null));
        attrs.add(AttributeBuilder.build(DESCR, (String) null));        
        attrs.add(AttributeBuilder.build(EMAIL, (String) null));
        attrs.add(AttributeBuilder.build(FAX, (String) null));
        attrs.add(AttributeBuilder.build(CUST_ID, (String) null));
        attrs.add(AttributeBuilder.build(SUPP_ID, (String) null));
        attrs.add(AttributeBuilder.build(SEC_ATTRS, (String) null));
        return attrs;
    }

    
    /**
     * @return the 
     */
    static public Set<Attribute> createRequiredAccountAttributes() {
        final Set<Attribute> attrs = CollectionUtil.newSet();       
        attrs.add(AttributeBuilder.build(Name.NAME, "TSTUSER"));
        attrs.add(AttributeBuilder.buildPassword("tstpwd".toCharArray()));
        attrs.add(AttributeBuilder.build(OWNER, "CUST"));
        return attrs;
    } 
}
