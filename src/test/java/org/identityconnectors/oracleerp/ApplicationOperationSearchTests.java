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

import static org.identityconnectors.oracleerp.OracleERPUtil.APPS_OC;
import static org.identityconnectors.oracleerp.OracleERPUtil.NAME;

import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.test.common.TestHelpers;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Attempts to test the {@link ApplicationOperationSearch}.
 *
 * @author petr
 * @since 1.0
 */
@Test(groups = { "integration" })
public class ApplicationOperationSearchTests extends OracleERPTestsBase {

    /**
     * Test method for
     * {@link ApplicationOperationSearch#executeQuery(ObjectClass, org.identityconnectors.dbcommon.FilterWhereBuilder, org.identityconnectors.framework.common.objects.ResultsHandler, OperationOptions)}
     * .
     */
    @Test
    public void testApplicationSearch() {
        final OracleERPConnector c = getConnector(CONFIG_SYSADM);
        final Set<Attribute> attrsOpt = getAttributeSet(ACCOUNT_OPTIONS);
        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        addAuditorDataOptions(oob, attrsOpt);

        final Filter filter =
                FilterBuilder.equalTo(AttributeBuilder.build(NAME, "Does no mather, not null"));
        List<ConnectorObject> results = TestHelpers.searchToList(c, APPS_OC, filter, oob.build());
        System.out.println(results);
        AssertJUnit.assertTrue("expect 1 connector object", results.size() == 1);
    }
}
