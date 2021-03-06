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

import static org.identityconnectors.oracleerp.OracleERPUtil.AUDITOR_RESPS;
import static org.identityconnectors.oracleerp.OracleERPUtil.FORM_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.FUNCTION_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.FUNCTION_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.MENU_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.MSG_COULD_NOT_READ;
import static org.identityconnectors.oracleerp.OracleERPUtil.OU_ID;
import static org.identityconnectors.oracleerp.OracleERPUtil.OU_NAME;
import static org.identityconnectors.oracleerp.OracleERPUtil.RESP_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RO_FORM_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.RO_FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RO_FUNCTIONS_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.RO_FUNCTION_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RO_USER_FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RW_FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RW_FUNCTION_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.RW_FUNCTION_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.RW_ONLY_FORM_IDS;
import static org.identityconnectors.oracleerp.OracleERPUtil.RW_USER_FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.SOB_ID;
import static org.identityconnectors.oracleerp.OracleERPUtil.SOB_NAME;
import static org.identityconnectors.oracleerp.OracleERPUtil.USER_FORM_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.USER_FUNCTION_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.USER_MENU_NAMES;
import static org.identityconnectors.oracleerp.OracleERPUtil.getColumn;
import static org.identityconnectors.oracleerp.OracleERPUtil.listToCommaDelimitedString;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.dbcommon.SQLUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * The Account User Responsibilities Update.
 *
 * @author Petr Jung
 * @version $Revision 1.0$
 * @since 1.0
 */
final class AuditorOperations extends Operation {

    private static final Log LOG = Log.getLog(AuditorOperations.class);

    /**
     * @param conn
     * @param cfg
     */
    AuditorOperations(OracleERPConnection conn, OracleERPConfiguration cfg) {
        super(conn, cfg);
    }

    /**
     *
     * Return Object of Auditor Data.
     *
     * List auditorResps (GO) userMenuNames menuIds userFunctionNames
     * functionIds formIds formNames userFormNames readOnlyFormIds
     * readWriteOnlyFormIds readOnlyFunctionIds readWriteOnlyFunctionIds
     * readOnlyFormNames readOnlyUserFormNames readWriteOnlyFormNames
     * readWriteOnlyUserFormNames
     *
     * @param amb
     *            AttributeMergeBuilder
     * @param respName
     *            String
     *
     */
    public void updateAuditorData(AttributeMergeBuilder amb, String respName) {
        final String method = "updateAuditorData";
        LOG.ok(method);
        // Profile Options used w/SOB and Organization
        String sobOption = "GL Set of Books ID";
        String ouOption = "MO: Operating Unit";

        String curResp = respName;
        String resp = null;
        String app = null;
        if (curResp != null) {
            StringTokenizer tok = new StringTokenizer(curResp, "||", false);
            if (tok != null && tok.countTokens() > 1) {
                resp = tok.nextToken();
                app = tok.nextToken();
            }
        }
        StringBuilder b = new StringBuilder();

        // one query
        b.append("SELECT DISTINCT 'N/A' userMenuName, 0 menuID, fffv.function_id,");
        b.append("fffv.user_function_name , ffv.form_id, ffv.form_name, ffv.user_form_name, ");
        b.append("fffv.function_name, ");
        b.append("fffv.parameters  FROM fnd_form_functions_vl fffv, ");
        b.append("fnd_form_vl ffv WHERE fffv.form_id=ffv.form_id(+) ");
        b.append("AND fffv.function_id NOT IN (SELECT action_id FROM fnd_resp_functions frf1 ");
        b.append("WHERE responsibility_id=(SELECT frv.responsibility_id ");
        b.append("FROM fnd_responsibility_vl frv , fnd_application_vl fa WHERE ");
        b.append("frv.application_id=fa.application_id AND  frv.responsibility_name=? ");
        b.append("AND fa.application_name=?) AND rule_type='F') ");
        b.append("AND function_id IN (SELECT function_id FROM fnd_menu_entries fme ");
        b.append("WHERE menu_id NOT IN (SELECT action_id FROM fnd_resp_functions ");
        b.append("WHERE responsibility_id=(SELECT frv.responsibility_id FROM fnd_responsibility_vl frv ");
        b.append(", fnd_application_vl fa WHERE frv.application_id=fa.application_id ");
        b.append("AND  frv.responsibility_name=? ");
        b.append("AND fa.application_name=?) AND rule_type='M')");
        b.append("START WITH menu_id=(SELECT frv.menu_id FROM fnd_responsibility_vl frv ");
        b.append(", fnd_application_vl fa WHERE frv.application_id=fa.application_id ");
        b.append("AND  frv.responsibility_name=? ");
        b.append("AND fa.application_name=?) CONNECT BY prior sub_menu_id=menu_id) ");
        b.append("UNION SELECT DISTINCT user_menu_name userMenuName, menu_id MenuID, ");
        b.append("0 function_id, 'N/A' user_function_name, 0 form_id, 'N/A' form_name, 'N/A' user_form_name, ");
        b.append(" 'N/A' function_name, ");
        b.append("'N/A' parameters  FROM fnd_menus_vl fmv WHERE menu_id IN (");
        b.append("SELECT menu_id FROM fnd_menu_entries fme WHERE menu_id NOT IN (");
        b.append("SELECT action_id FROM fnd_resp_functions WHERE responsibility_id=(");
        b.append("SELECT frv.responsibility_id FROM fnd_responsibility_vl frv, fnd_application_vl fa ");
        b.append("WHERE frv.application_id=fa.application_id AND frv.responsibility_name=? ");
        b.append("AND fa.application_name=?) ");
        b.append("AND rule_type='M') START WITH menu_id=(SELECT frv.menu_id ");
        b.append("FROM fnd_responsibility_vl frv , fnd_application_vl fa WHERE ");
        b.append("frv.application_id=fa.application_id AND  frv.responsibility_name=? ");
        b.append("AND fa.application_name=?) ");
        b.append("CONNECT BY prior sub_menu_id=menu_id) ORDER BY 2,4");
        // one query
        LOG.ok(method + ": Resp = " + curResp);

        PreparedStatement st = null;
        ResultSet res = null;

        List<String> menuIds = new ArrayList<String>();
        List<String> menuNames = new ArrayList<String>();
        List<String> functionIds = new ArrayList<String>();
        List<String> userFunctionNames = new ArrayList<String>();
        List<String> roFormIds = new ArrayList<String>();
        List<String> rwFormIds = new ArrayList<String>();
        List<String> roFormNames = new ArrayList<String>();
        List<String> rwFormNames = new ArrayList<String>();
        List<String> roUserFormNames = new ArrayList<String>();
        List<String> rwUserFormNames = new ArrayList<String>();
        List<String> roFunctionNames = new ArrayList<String>();
        List<String> rwFunctionNames = new ArrayList<String>();
        List<String> roFunctionIds = new ArrayList<String>();
        List<String> rwFunctionIds = new ArrayList<String>();

        // objects to collect all read/write functions and related info
        // which is used later for false positive fix-up
        Map<String, Map<String, Object>> functionIdMap = new HashMap<String, Map<String, Object>>();
        Map<String, Object> attrMap = new HashMap<String, Object>();

        try {

            st = getConn().prepareStatement(b.toString());
            st.setString(1, resp);
            st.setString(2, app);
            st.setString(3, resp);
            st.setString(4, app);
            st.setString(5, resp);
            st.setString(6, app);
            st.setString(7, resp);
            st.setString(8, app);
            st.setString(9, resp);
            st.setString(10, app);
            res = st.executeQuery();

            while (res != null && res.next()) {

                String menuName = getColumn(res, 1);
                if (menuName != null && !menuName.equals("N/A")) {
                    menuNames.add(menuName);
                }
                String menuId = getColumn(res, 2);
                if (menuId != null && !menuId.equals("0")) {
                    menuIds.add(menuId);
                }
                String funId = getColumn(res, 3);
                if (funId != null && !funId.equals("0")) {
                    functionIds.add(funId);
                }
                String funName = getColumn(res, 4);
                if (funName != null && !funName.equals("N/A")) {
                    userFunctionNames.add(funName);
                }
                String param = getColumn(res, 9); // column added for parameters
                boolean qo = false;
                if (param != null) {
                    // pattern can be QUERY_ONLY=YES, QUERY_ONLY = YES,
                    // QUERY_ONLY="YES",
                    // QUERY_ONLY=Y, etc..
                    Pattern pattern = Pattern.compile("\\s*QUERY_ONLY\\s*=\\s*\"*Y");
                    Matcher matcher = pattern.matcher(param.toUpperCase());
                    if (matcher.find()) {
                        qo = true;
                    }
                }
                if (qo) {
                    String roFunId = getColumn(res, 3);
                    if (roFunId != null && !roFunId.equals("0")) {
                        roFunctionIds.add(roFunId);
                    }
                    String roFunctionName = getColumn(res, 8);
                    if (roFunctionName != null && !roFunctionName.equals("N/A")) {
                        roFunctionNames.add(roFunctionName);
                    }
                    String roFormId = getColumn(res, 5);
                    if (roFormId != null && !roFormId.equals("0")) {
                        roFormIds.add(roFormId);
                    }
                    String roFormName = getColumn(res, 6);
                    if (roFormName != null && !roFormName.equals("N/A")) {
                        roFormNames.add(roFormName);
                    }
                    String roUserFormName = getColumn(res, 7);
                    if (roUserFormName != null && !roUserFormName.equals("N/A")) {
                        roUserFormNames.add(roUserFormName);
                    }
                } else {
                    String rwFunId = getColumn(res, 3);
                    if (rwFunId != null && !rwFunId.equals("0")) {
                        rwFunctionIds.add(rwFunId);
                    }
                    String rwFunctionName = getColumn(res, 8);
                    if (rwFunctionName != null && !rwFunctionName.equals("N/A")) {
                        rwFunctionNames.add(rwFunctionName);
                        attrMap.put("rwFunctionName", rwFunctionName);
                    }
                    String rwFormId = getColumn(res, 5);
                    if (rwFormId != null && !rwFormId.equals("0")) {
                        rwFormIds.add(rwFormId);
                        attrMap.put("rwFormId", rwFormId);
                    }
                    String rwFormName = getColumn(res, 6);
                    if (rwFormName != null && !rwFormName.equals("N/A")) {
                        rwFormNames.add(rwFormName);
                        attrMap.put("rwFormName", rwFormName);
                    }
                    String rwUserFormName = getColumn(res, 7);
                    if (rwUserFormName != null && !rwUserFormName.equals("N/A")) {
                        rwUserFormNames.add(rwUserFormName);
                        attrMap.put("rwUserFormName", rwUserFormName);
                    }
                    if (!attrMap.isEmpty()) {
                        functionIdMap.put(rwFunId, new HashMap<String, Object>(attrMap));
                        attrMap.clear();
                    }
                } // end-if (qo)
            } // end-while
             // no catch, just use finally to ensure closes happen
        } catch (ConnectorException e) {
            final String msg = getCfg().getMessage(MSG_COULD_NOT_READ);
            LOG.error(e, msg);
            SQLUtil.rollbackQuietly(getConn());
            throw e;
        } catch (Exception e) {
            final String msg = getCfg().getMessage(MSG_COULD_NOT_READ);
            LOG.error(e, msg);
            SQLUtil.rollbackQuietly(getConn());
            throw new ConnectorException(msg, e);
        } finally {
            SQLUtil.closeQuietly(res);
            res = null;
            SQLUtil.closeQuietly(st);
            st = null;
        }

        // Post Process Results looking for false-positive (misidentified rw
        // objects) only if
        // there are any read only functions (roFunctionIds != null)
        // The results of this query are additional roFunctionIds by following
        // logic
        // in bug#13405.
        if (roFunctionIds != null && roFunctionIds.size() > 0) {
            b = new StringBuilder();
            b.append("SELECT function_id from fnd_compiled_menu_functions ");
            b.append("WHERE menu_id IN ");
            b.append("( SELECT sub_menu_id from fnd_menu_entries ");
            b.append("WHERE function_id IN (");
            b.append(listToCommaDelimitedString(roFunctionIds));
            b.append(") AND sub_menu_id > 0 AND grant_flag = 'Y' ");
            b.append("AND sub_menu_id IN (");
            b.append(listToCommaDelimitedString(menuIds));
            b.append(") )");
            try {
                st = getConn().prepareStatement(b.toString());
                res = st.executeQuery();
                while (res != null && res.next()) {
                    // get each functionId and use as key to find associated rw
                    // objects
                    // remove from rw bucket and place in ro bucket
                    String functionId = getColumn(res, 1);
                    if (functionId != null) {
                        Map<String, Object> idObj = functionIdMap.get(functionId);
                        if (idObj != null) {
                            if (rwFunctionIds.contains(functionId)) {
                                rwFunctionIds.remove(functionId);
                                roFunctionIds.add(functionId);
                            }
                            String rwFunctionName = (String) idObj.get("rwFunctionName");
                            if (rwFunctionNames.contains(rwFunctionName)) {
                                rwFunctionNames.remove(rwFunctionName);
                                roFunctionNames.add(rwFunctionName);
                            }
                            String rwFormId = (String) idObj.get("rwFormId");
                            if (rwFormIds.contains(rwFormId)) {
                                rwFormIds.remove(rwFormId);
                                roFormIds.add(rwFormId);
                            }
                            String rwFormName = (String) idObj.get("rwFormName");
                            if (rwFormNames.contains(rwFormName)) {
                                rwFormNames.remove(rwFormName);
                                roFormNames.add(rwFormName);
                            }
                            String rwUserFormName = (String) idObj.get("rwUserFormName");
                            if (rwUserFormNames.contains(rwUserFormName)) {
                                rwUserFormNames.remove(rwUserFormName);
                                roUserFormNames.add(rwUserFormName);
                            }
                        } // if idObj ! null
                    } // if functionId != null
                } // end while

                // no catch, just use finally to ensure closes happen
            } catch (ConnectorException e) {
                final String msg = getCfg().getMessage(MSG_COULD_NOT_READ);
                LOG.error(e, msg);
                SQLUtil.rollbackQuietly(getConn());
                throw e;
            } catch (Exception e) {
                final String msg = getCfg().getMessage(MSG_COULD_NOT_READ);
                LOG.error(e, msg);
                SQLUtil.rollbackQuietly(getConn());
                throw new ConnectorException(msg, e);
            } finally {
                SQLUtil.closeQuietly(res);
                res = null;
                SQLUtil.closeQuietly(st);
                st = null;
            }
        } // end-if roFunctionIds has contents

        // create objects and load auditor data
        List<String> userFormNameList = new ArrayList<String>(roUserFormNames);
        userFormNameList.addAll(rwUserFormNames);
        List<String> formNameList = new ArrayList<String>(roFormNames);
        formNameList.addAll(rwFormNames);
        List<String> formIdList = new ArrayList<String>(roFormIds);
        formIdList.addAll(rwFormIds);
        List<String> functionNameList = new ArrayList<String>(roFunctionNames);
        functionNameList.addAll(rwFunctionNames);
        List<String> functionIdsList = new ArrayList<String>(roFunctionIds);
        functionIdsList.addAll(rwFunctionIds);

        amb.addAttribute(USER_MENU_NAMES, menuNames);
        amb.addAttribute(MENU_IDS, menuIds);
        amb.addAttribute(USER_FUNCTION_NAMES, userFunctionNames);
        amb.addAttribute(FUNCTION_IDS, functionIdsList);
        amb.addAttribute(RO_FUNCTIONS_IDS, roFunctionIds);
        amb.addAttribute(RW_FUNCTION_IDS, rwFunctionIds);
        amb.addAttribute(FORM_IDS, formIdList);
        amb.addAttribute(RO_FORM_IDS, roFormIds);
        amb.addAttribute(RW_ONLY_FORM_IDS, rwFormIds);
        amb.addAttribute(FORM_NAMES, formNameList);
        amb.addAttribute(RO_FORM_NAMES, roFormNames);
        amb.addAttribute(RW_FORM_NAMES, rwFormNames);
        amb.addAttribute(USER_FORM_NAMES, userFormNameList);
        amb.addAttribute(RO_USER_FORM_NAMES, roUserFormNames);
        amb.addAttribute(RW_USER_FORM_NAMES, rwUserFormNames);
        amb.addAttribute(FUNCTION_NAMES, functionNameList);
        amb.addAttribute(RO_FUNCTION_NAMES, roFunctionNames);
        amb.addAttribute(RW_FUNCTION_NAMES, rwFunctionNames);
        final String respNameConn = resp + "||" + app;
        amb.addAttribute(RESP_NAMES, respNameConn);
        amb.addAttribute(AUDITOR_RESPS, respNameConn);

        // check to see if SOB/ORGANIZATION is required
        if (getCfg().isReturnSobOrgAttrs()) {
            b = new StringBuilder();
            // query for SOB / Organization
            b.append("Select distinct ");
            b.append("decode(fpo1.user_profile_option_name, '");
            b.append(sobOption
                    + "', fpo1.user_profile_option_name||'||'||gsob.name||'||'||gsob.set_of_books_id, '");
            b.append(ouOption
                    + "', fpo1.user_profile_option_name||'||'||hou1.name||'||'||hou1.organization_id)");
            b.append(" from " + getCfg().app() + "fnd_responsibility_vl fr, " + getCfg().app()
                    + "fnd_profile_option_values fpov, " + getCfg().app()
                    + "fnd_profile_options fpo");
            b.append(" , " + getCfg().app() + "fnd_profile_options_vl fpo1, " + getCfg().app()
                    + "hr_organization_units hou1, " + getCfg().app() + "gl_sets_of_books gsob");
            b.append(" where fr.responsibility_id = fpov.level_value and gsob.set_of_books_id = fpov.profile_option_value");
            b.append(" and  fpo.profile_option_name = fpo1.profile_option_name and fpo.profile_option_id = fpov.profile_option_id");
            b.append(" and  fpo.application_id = fpov.application_id and   fpov.profile_option_value = to_char(hou1.organization_id(+))");
            b.append(" and  fpov.profile_option_value = to_char(gsob.set_of_books_id(+)) and   fpov.level_id = 10003");
            b.append(" and  fr.responsibility_name = ?");
            b.append(" order by 1");

            LOG.ok(method + ": Resp = " + curResp);

            try {
                st = getConn().prepareStatement(b.toString());
                st.setString(1, resp);
                res = st.executeQuery();

                while (res != null && res.next()) {
                    String option = getColumn(res, 1);
                    if (option != null && option.startsWith(sobOption)) {
                        List<String> values = Arrays.asList(option.split("||"));
                        if (values != null && values.size() == 3) {
                            amb.addAttribute(SOB_NAME, values.get(1));
                            amb.addAttribute(SOB_ID, values.get(2));
                        }
                    } else if (option != null && option.startsWith(ouOption)) {
                        List<String> values = Arrays.asList(option.split("||"));
                        if (values != null && values.size() == 3) {
                            amb.addAttribute(OU_NAME, values.get(1));
                            amb.addAttribute(OU_ID, values.get(2));
                        }
                    }
                }
            } catch (Exception e) {
                final String msg = getCfg().getMessage(MSG_COULD_NOT_READ);
                LOG.error(e, msg);
                SQLUtil.rollbackQuietly(getConn());
            } finally {
                SQLUtil.closeQuietly(res);
                res = null;
                SQLUtil.closeQuietly(st);
                st = null;
            }
        }

        LOG.ok(method + " done");
    }
}
