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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.oracleerp;

import static org.identityconnectors.oracleerp.OracleERPUtil.OWNER;
import static org.identityconnectors.oracleerp.OracleERPUtil.getAttributeInfos;

import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.test.common.TestHelpers;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Attempts to test the {@link OracleERPConnector} with the framework.
 *
 * @author petr
 * @since 1.0
 */
@Test(groups = { "integration" })
public class AccountOperationSearchTests extends OracleERPTestsBase {

    /**
     * Test method .
     */
    @Test
    public void testQueryAuditorData() {
        final OracleERPConnector c = getConnector(CONFIG_SYSADM);
        final Set<Attribute> attrs = getAttributeSet(ACCOUNT_ALL_ATTRS);
        replaceNameByRandom(attrs);
        final Set<Attribute> attrsOpt = getAttributeSet(ACCOUNT_OPTIONS);

        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        addAuditorDataOptions(oob, attrsOpt);
        addAllAttributesToGet(oob, getAttributeInfos(c.schema(), ObjectClass.ACCOUNT_NAME));

        final Uid uid = c.create(ObjectClass.ACCOUNT, attrs, null);
        AssertJUnit.assertNotNull(uid);

        List<ConnectorObject> results =
                TestHelpers.searchToList(c, ObjectClass.ACCOUNT, FilterBuilder.equalTo(uid), oob
                        .build());
        AssertJUnit.assertTrue("expect 1 connector object", results.size() == 1);
        final ConnectorObject co = results.get(0);
        final Set<Attribute> returned = co.getAttributes();
        System.out.println(returned);

        testAttrSet(attrs, returned, OperationalAttributes.PASSWORD_NAME, OWNER,
                OperationalAttributes.PASSWORD_EXPIRED_NAME);

        final Set<Attribute> auditorAttr = getAttributeSet(ACCOUNT_AUDITOR);
        testAttrSet(auditorAttr, returned);
    }

    /**
     * Test method.
     */
    @Test
    public void testSearchValidAccount() {
        final OracleERPConnector c = getConnector(CONFIG_TST);
        final Set<Attribute> attrs = getAttributeSet(ACCOUNT_REQUIRED_ATTRS);
        replaceNameByRandom(attrs);
        final Set<Attribute> attrsOpt = getAttributeSet(ACCOUNT_OPTIONS);

        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        addAuditorDataOptions(oob, attrsOpt);
        addAllAttributesToGet(oob, getAttributeInfos(c.schema(), ObjectClass.ACCOUNT_NAME));

        final Uid uid = c.create(ObjectClass.ACCOUNT, attrs, null);
        AssertJUnit.assertNotNull(uid);

        List<ConnectorObject> results =
                TestHelpers.searchToList(c, ObjectClass.ACCOUNT, FilterBuilder.equalTo(uid), oob
                        .build());
        AssertJUnit.assertTrue("expect 1 connector object", results.size() == 1);
        final ConnectorObject co = results.get(0);
        final Set<Attribute> returned = co.getAttributes();
        System.out.println(returned);

        testAttrSet(attrs, returned, OperationalAttributes.PASSWORD_NAME);
        final Set<Attribute> enabledAttr = getAttributeSet(ACCOUNT_ENABLED);
        testAttrSet(enabledAttr, returned, OperationalAttributes.ENABLE_DATE_NAME,
                OperationalAttributes.PASSWORD_EXPIRED_NAME);
    }

    /**
     * Test method.
     */
    @Test
    public void testSearchLowerCaseValidAccount() {
        final OracleERPConnector c = getConnector(CONFIG_TST);
        final Set<Attribute> attrs = getAttributeSet(ACCOUNT_REQUIRED_ATTRS);
        replaceNameByRandom(attrs);

        final Uid uid = c.create(ObjectClass.ACCOUNT, attrs, null);
        AssertJUnit.assertNotNull(uid);

        List<ConnectorObject> results =
                TestHelpers.searchToList(c, ObjectClass.ACCOUNT, FilterBuilder.equalTo(uid), null);
        AssertJUnit.assertTrue("expect 1 connector object", results.size() == 1);

        List<ConnectorObject> results1 =
                TestHelpers.searchToList(c, ObjectClass.ACCOUNT, FilterBuilder.equalTo(new Uid(uid
                        .getUidValue().toLowerCase())), null);
        AssertJUnit.assertTrue("expect 1 connector object", results1.size() == 1);

        final ConnectorObject co = results1.get(0);
        final Set<Attribute> returned = co.getAttributes();
        System.out.println(returned);

        testAttrSet(attrs, returned, OperationalAttributes.PASSWORD_NAME);
    }
}
