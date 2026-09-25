package com.netpilot.util;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnmpUtil {

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RETRIES = 2;

    private static Snmp createSnmp() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
        return snmp;
    }

    private static CommunityTarget createTarget(String ip, String community, int port) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(new org.snmp4j.smi.UdpAddress(ip + "/" + port));
        target.setRetries(DEFAULT_RETRIES);
        target.setTimeout(DEFAULT_TIMEOUT);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    public static String get(String ip, String community, String oid) {
        return get(ip, community, oid, 161);
    }

    public static String get(String ip, String community, String oid, int port) {
        try (Snmp snmp = createSnmp()) {
            PDU pdu = new DefaultPDUFactory().createPDU(PDU.GET);
            pdu.add(new VariableBinding(new OID(oid)));

            CommunityTarget target = createTarget(ip, community, port);
            ResponseEvent event = snmp.send(pdu, target);

            if (event.getResponse() != null) {
                VariableBinding vb = event.getResponse().get(0);
                if (vb != null && vb.getVariable() != null) {
                    return vb.getVariable().toString();
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 遍历 SNMP 表（使用 GETNEXT 连续获取）
     */
    public static List<String> getNext(String ip, String community, String oid) {
        return getNext(ip, community, oid, 161);
    }

    public static List<String> getNext(String ip, String community, String oid, int port) {
        List<String> results = new ArrayList<>();
        try (Snmp snmp = createSnmp()) {
            CommunityTarget target = createTarget(ip, community, port);
            OID currentOID = new OID(oid);
            OID endOID = new OID(oid + ".0"); // 简单的结束条件

            for (int i = 0; i < 100; i++) { // 最多遍历 100 条
                PDU pdu = new DefaultPDUFactory().createPDU(PDU.GETNEXT);
                pdu.add(new VariableBinding(currentOID));

                ResponseEvent event = snmp.send(pdu, target);
                if (event.getResponse() == null) {
                    break;
                }

                VariableBinding vb = event.getResponse().get(0);
                if (vb == null || vb.getVariable() == null) {
                    break;
                }

                OID resultOID = vb.getOid();
                // 如果返回的 OID 不再以指定前缀开头，说明遍历结束
                if (!resultOID.toString().startsWith(oid)) {
                    break;
                }

                results.add(vb.getVariable().toString());
                currentOID = resultOID;
            }
            return results;
        } catch (IOException e) {
            return results;
        }
    }

    public static Double getCpuUsage(String ip, String community) {
        String[] oids = {
                ".1.3.6.1.4.1.2011.6.1.2.0",
                ".1.3.6.1.4.1.9.9.109.1.1.1.1.5",
                ".1.3.6.1.4.1.2021.10.1.3.1",
                ".1.3.6.1.4.1.2021.10.1.3.2"
        };
        for (String oid : oids) {
            String value = get(ip, community, oid);
            if (value != null && !value.isEmpty()) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    // 不是数字，跳过
                }
            }
        }
        return null;
    }

    public static Double getMemoryUsage(String ip, String community) {
        String[] oids = {
                ".1.3.6.1.4.1.2011.6.1.3.0",
                ".1.3.6.1.4.1.9.9.48.1.1.1.1.2",
                ".1.3.6.1.4.1.2021.4.11.0",
                ".1.3.6.1.4.1.2021.4.13.0"
        };
        for (String oid : oids) {
            String value = get(ip, community, oid);
            if (value != null && !value.isEmpty()) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    // 不是数字，跳过
                }
            }
        }
        return null;
    }
}