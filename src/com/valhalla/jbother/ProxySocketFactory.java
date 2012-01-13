/*
 Copyright 2005 Mike Radin

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 1, or (at your option)
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package com.valhalla.jbother;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import javax.net.SocketFactory;

/**
 *Allows connections to SOCKS4 and SOCKS5 proxy servers. This is not a generic implementation of either protocol, it was developed specifically for JBother and supports only a subset of features of the proxy protocols.
 *
 *@author Mike Radin - mike@projecttree.com
**/
public class ProxySocketFactory extends SocketFactory{
    public static final int SOCKS4 = 1;
    public static final int SOCKS5 = 2;
    
    public static final int SOCKS5PASSWD = 2; // SOCKS5 password authentication

    private String proxyhost;
    private int proxyport;
    private String proxyuser = "";
    private String proxypasswd = "";
    private int proxytype = SOCKS4;

    public ProxySocketFactory(String host, int port){
        proxyhost = host;
        proxyport = port;
    }
    public ProxySocketFactory(String host, int port, int type, String user, String passwd){
        proxyhost = host;
        proxyport = port;
        proxytype = type;
        proxyuser = user;
        proxypasswd = passwd;
    }
    public Socket createSocket(InetAddress host, int port) throws IOException{
        return createProxySocket(host.getHostAddress(), port);
    }
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException{
        return createProxySocket(address.getHostAddress(), port);
    }
    public Socket createSocket(String host, int port) throws IOException{
        return createProxySocket(host, port);
    }
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException{
        return createProxySocket(host, port);
    }
    private Socket createProxySocket(String host, int port) throws IOException{
        Socket proxysocket = new Socket(proxyhost, proxyport);
        proxysocket.setTcpNoDelay(true);

        try{
            InputStream is = proxysocket.getInputStream();
            OutputStream os = proxysocket.getOutputStream();

            if(proxytype == SOCKS4){
                connectSOCKS4(is, os, host, port, proxyuser);
            }else if(proxytype == SOCKS5){
                int authtype = 0;
                if(proxyuser != null && proxyuser.length() > 0){ authtype = querySOCKS5(is, os, new byte[] {0x0, 0x2}); }
                else{ authtype = querySOCKS5(is, os, new byte[] {0x0}); }
                if(authtype == SOCKS5PASSWD){ authenticateSOCKS5(is, os, authtype, proxyuser, proxypasswd); }
                connectSOCKS5(is, os, host, port);
            }else{
                throw new IOException("ProxySocketFactory: unsupported proxy type");
            }
        }catch (SocketException e){
            throw new IOException("Error communicating with SOCKS4 server " + proxyhost + ":" + proxyport + ", " + e.getMessage());
        }catch(IOException e){
            System.err.println(e);
            throw e;
        }
        return proxysocket;
    }
    private void connectSOCKS4(InputStream is, OutputStream os, String host, int port, String user) throws IOException{
        int i = ((user != null) ? user.length() : 0);
        byte[] socks4msg = new byte[9 + i];
        socks4msg[0] = 0x4;
        socks4msg[1] = 0x1;
        socks4msg[2] = (byte)(port >> 8);
        socks4msg[3] = (byte)port;
        System.arraycopy(InetAddress.getByName(host).getAddress(), 0, socks4msg, 4, 4);
        if(user != null && user.length() > 0){ System.arraycopy(user.getBytes(), 0, socks4msg, 8, user.length()); }
        socks4msg[8 + i] = (byte)0;

        os.write(socks4msg);
        os.flush();

        byte[] socks4rep = new byte[8];
        is.read(socks4rep, 0, 8);

        if(socks4rep[0] != (byte)0){ throw new IOException("Invalid response from SOCKS4 server " + proxyhost + ":" + proxyport); }
        if(socks4rep[1] != (byte)90){ throw new IOException("SOCKS4 server unable to connect, reason: " + socks4rep[1]); }

        return;
    }
    private int querySOCKS5(InputStream is, OutputStream os, byte[] types) throws IOException{
        byte[] socks5msg = new byte[2 + types.length];
        socks5msg[0] = 0x5;
        socks5msg[1] = (byte)types.length;
        System.arraycopy(types, 0, socks5msg, 2, types.length);

        os.write(socks5msg);
        os.flush();

        byte[] socks5rep = new byte[2];
        is.read(socks5rep, 0, 2);

        if(socks5rep[1] == 0xFF){ throw new IOException("SOCKS5: no acceptable authentication methods."); }

        return (int)socks5rep[1];
    }
    private void authenticateSOCKS5(InputStream is, OutputStream os, int type, String user, String passwd) throws IOException{
        int ulen = ((user != null) ? user.length(): 0);
        int plen = ((passwd != null) ? passwd.length(): 0);
        byte[] socks5msg = new byte[3 + ulen + plen];
        socks5msg[0] = 0x1;
        socks5msg[1] = (byte)ulen;
        if(ulen > 0){ System.arraycopy(user.getBytes(), 0, socks5msg, 2, ulen); }
        socks5msg[2 + ulen] = (byte)plen;
        if(plen > 0){ System.arraycopy(passwd.getBytes(), 0, socks5msg, 3 + ulen, plen);}

        os.write(socks5msg);
        os.flush();

        byte[] socks5rep = new byte[2];
        is.read(socks5rep, 0, 2);

        if(socks5rep[1] != 0x0){ throw new IOException("SOCKS5 authentication failed, reason: " + socks5rep[1]); }

        return;
    }
    private void connectSOCKS5(InputStream is, OutputStream os, String host, int port) throws IOException{
        byte[] socks5msg = new byte[10];
        socks5msg[0] = 0x5;
        socks5msg[1] = 0x1;
        socks5msg[2] = 0x0;
        socks5msg[3] = 0x1;
        System.arraycopy(InetAddress.getByName(host).getAddress(), 0, socks5msg, 4, 4);
        socks5msg[8] = (byte)(port >> 8);
        socks5msg[9] = (byte)port;

        os.write(socks5msg);
        os.flush();

        byte[] socks5rep = new byte[10];
        is.read(socks5rep, 0, 10);

        if(socks5rep[1] != 0x0){ throw new IOException("SOCKS5 server unable to connect, reason: " + socks5rep[1]); }

        return;
    }
}
