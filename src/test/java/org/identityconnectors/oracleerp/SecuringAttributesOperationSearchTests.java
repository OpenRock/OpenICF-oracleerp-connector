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

import static org.identityconnectors.oracleerp.OracleERPUtil.NAME;
import static org.identityconnectors.oracleerp.OracleERPUtil.SEC_ATTRS_OC;

import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.test.common.TestHelpers;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Attempts to test the {@link SecuringAttributesOperationSearch}.
 *
 * @author petr
 * @since 1.0
 */
public class SecuringAttributesOperationSearchTests extends OracleERPTestsBase {
    /**
     * Test method.
     */
    @Test(groups = { "integration" })
    public void testSecuringAttributesOperationSearch() {
        final OracleERPConnector c = getConnector(CONFIG_SYSADM);
        if (!c.getCfg().isNewResponsibilityViews()) {
            return;
        }
        final Set<Attribute> attrsOpt = getAttributeSet(ACCOUNT_OPTIONS);
        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        addAuditorDataOptions(oob, attrsOpt);

        final Filter filter =
                FilterBuilder.equalTo(AttributeBuilder.build(NAME, "Does no mather, not null"));
        List<ConnectorObject> results =
                TestHelpers.searchToList(c, SEC_ATTRS_OC, filter, oob.build());
        System.out.println(results);
        AssertJUnit.assertEquals("connector object size", 13, results.size());
    }
}
