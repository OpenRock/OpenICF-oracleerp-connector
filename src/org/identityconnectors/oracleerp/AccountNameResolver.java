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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.dbcommon.DatabaseQueryBuilder;
import org.identityconnectors.dbcommon.FilterWhereBuilder;
import org.identityconnectors.dbcommon.SQLParam;
import org.identityconnectors.dbcommon.SQLUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.operations.SearchOp;

/**
 * The Account CreateOp implementation of the SPI Select attributes from fnd_user table, add person details from
 * PER_PEOPLE_F table add responsibility names add auditor data add securing attributes all filtered according
 * attributes to get
 * 
 * @author Petr Jung
 * @version $Revision 1.0$
 * @since 1.0
 */
final class AccountNameResolver implements NameResolver {

    /**
     * Setup logging.
     */
    static final Log log = Log.getLog(AccountNameResolver.class);

    /**
     * Map column name to attribute name, special attributes are handed separated
     * 
     * @param columnName
     * @return the columnName
     */
    public String getAttributeName(String columnName) {
        if (FULL_NAME.equalsIgnoreCase(columnName)) {
            return PERSON_FULLNAME;
        }
        return columnName;
    }

    /**
     * Map the attribute name to column name, including the special attributes
     * 
     * @param attributeName
     * @return the columnName
     */
    public String getColumnName(String attributeName) {
        if (Name.NAME.equalsIgnoreCase(attributeName)) {
            return USER_NAME;
        } else if (Uid.NAME.equalsIgnoreCase(attributeName)) {
            return USER_NAME;
        } else if (PERSON_FULLNAME.equalsIgnoreCase(attributeName)) {
            return FULL_NAME;
        }
        return attributeName;
    }

}
