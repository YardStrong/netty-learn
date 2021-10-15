/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package online.yardstrong.netty.utils;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * learn from dolphin scheduler
 * @author yardstrong
 */
public class IpUtil {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(IpUtil.class);

    /**
     * Ipv4地址正则表达式
     */
    private static final String IP_REGEX = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

    /**
     * UNKNOWN
     */
    private static String LOCAL_HOST = "unknown";

    static {
        String host = System.getenv("HOSTNAME");
        if (isNotEmpty(host)) {
            LOCAL_HOST = host;
        } else {
            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                if (isNotEmpty(hostName)) {
                    LOCAL_HOST = hostName;
                }
            } catch (UnknownHostException e) {
                LOG.error("Failed to get hostname", e);
            }
        }
    }

    /**
     * 获取本机hostname
     * @return hostname
     */
    public static String getLocalHost() {
        return LOCAL_HOST;
    }


    /**
     * 获取第一个非环网卡
     * @return IP
     */
    public static String getFirstNoLoopBackIpv4Address() {
        Collection<String> allNoLoopBackIpv4Addresses = getNoLoopBackIpv4Addresses();
        if (allNoLoopBackIpv4Addresses.isEmpty()) {
            return null;
        }
        return allNoLoopBackIpv4Addresses.iterator().next();
    }

    /**
     * 获取非环网卡
     * @return IP
     */
    public static Collection<String> getNoLoopBackIpv4Addresses() {
        Collection<String> noLoopBackIpv4Addresses = new ArrayList<>();
        Collection<InetAddress> allInetAddresses = getAllHostAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()
                    && !(address instanceof Inet6Address)) {
                noLoopBackIpv4Addresses.add(address.getHostAddress());
            }
        }
        if (noLoopBackIpv4Addresses.isEmpty()) {
            for (InetAddress address : allInetAddresses) {
                if (!address.isLoopbackAddress() && !(address instanceof Inet6Address)) {
                    noLoopBackIpv4Addresses.add(address.getHostAddress());
                }
            }
        }
        return noLoopBackIpv4Addresses;
    }

    /**
     * 获取所有网卡address
     * @return 地址
     */
    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据主机名获取ip
     * @param host host
     * @return ip
     */
    public static String getIpByHostName(String host) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            LOG.error("get IP error", e);
        }
        if (address == null) {
            return "";
        }
        return address.getHostAddress();

    }

    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 是否是ipv4
     * @param ip ip字符串
     * @return boolean
     */
    public static boolean isIpv4(String ip) {
        if (null == ip || ip.length() < 7 || ip.length() > 15) {
            return false;
        }

        Pattern pat = Pattern.compile(IP_REGEX);

        return pat.matcher(ip).find();
    }
}
