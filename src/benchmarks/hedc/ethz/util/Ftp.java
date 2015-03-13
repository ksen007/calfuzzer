/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * The interface to sun.net.ftp was deciphered by 
 * Elliotte Rusty Harold / Secret Java
 *
 * $Id: Ftp.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 24/02/00  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import sun.net.ftp.FtpClient;
import java.net.*;
import java.io.*;
import java.util.*;


public class Ftp extends FtpClient implements Cloneable {

    private static final int FTP_SUCCESS = 1;
    private static final int FTP_TRY_AGAIN = 2;
    private static final int FTP_ERROR = 3;
    private Socket dataSocket_ = null;
    private ServerSocket serverSocket_ = null;


    /** 
     * New FullFtpClient connected to host <i>host</i>. 
     */
    public Ftp(String host) throws IOException {
	super(host);
    }
    
    /** 
     * New FullFtpClient connected to host <i>host</i>, port <i>port</i>. 
     */
    public Ftp(String host, int port) throws IOException {
	super(host, port);
    }
    
    /** 
     * Move up one directory in the ftp file system 
     */
    public void cdup() throws IOException {
	issueCommandCheck("CDUP");
    }
    
    /** 
     * Create a new directory named s in the ftp file system 
     */
    public void mkdir(String s) throws IOException {
	issueCommandCheck("MKDIR " + s);
    }
    
    /** 
     * Delete the specified directory from the ftp file system 
     */
    public void rmdir(String s) throws IOException {
	issueCommandCheck("RMD " + s);
    }
    
    /**
     * Delete the file s from the ftp file system 
     */
    public void delete(String s) throws IOException {
	issueCommandCheck("DELE " + s);
    }
    
    /** 
     * Get the name of the present working directory on the ftp file system 
     */
    public String pwd() throws IOException {
	issueCommandCheck("PWD");
	StringBuffer result = new StringBuffer();
	for (Enumeration e = serverResponse.elements(); e.hasMoreElements();) {
	    result.append((String) e.nextElement());
	}
	return result.toString();
    }

    /**
     * List file names
     */
    public Enumeration nlst(String s) throws IOException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        byte ab[] = inetAddress.getAddress();
        serverSocket_ = new ServerSocket(0, 1);
	StringBuffer sb = new StringBuffer(32); 
	sb.append("PORT ");
        for (int i = 0; i < ab.length; i++) {
            sb.append(String.valueOf(ab[i] & 255));
	    sb.append(",");
	}
        sb.append(String.valueOf(serverSocket_.getLocalPort() >>> 8 & 255));
	sb.append(",");
	sb.append(String.valueOf(serverSocket_.getLocalPort() & 255));
	if (issueCommand(sb.toString()) != FTP_SUCCESS) {
            serverSocket_.close();
	    throw new IOException(getResponseString());
        } else if (issueCommand("NLST " + ((s == null) ? "" : s)) != FTP_SUCCESS) {
            serverSocket_.close();
	    throw new IOException(getResponseString());
        }
        dataSocket_ = serverSocket_.accept();
        serverSocket_.close();
	serverSocket_ = null;
	Vector v = readServerResponse_(dataSocket_.getInputStream());
	dataSocket_.close();
	dataSocket_ = null;
	return (v == null) ? null : v.elements();
    }
    
    public void closeServer() {
	try {
	    if (serverSocket_ != null)
		serverSocket_.close();
	    if (dataSocket_ != null) 
		dataSocket_.close();
	} catch(Exception e) {
	    e.printStackTrace();
	} finally {
	    serverSocket_ = null;
	    dataSocket_ = null;
	    try {
		super.closeServer();
	    } catch (Exception e) {}
	}
    }
	

    private Vector readServerResponse_(InputStream is) throws IOException {
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
	Vector ret = new Vector();
	String line = br.readLine();
	while (line != null) {
	    ret.addElement(line);
	    line = br.readLine();
	}
	return ret;
    }

    private void printResponse_() {
	Enumeration e = (serverResponse == null)  ? null : serverResponse.elements();
	for (; e != null && e.hasMoreElements(); ) {
	    System.out.println(e.nextElement());
	}
	System.out.println("no server response");
    }

    public void login() throws IOException {
	login("anonymous", "hedc@inf.ethz.ch");
    }
    

    /** 
     * for test
     */
    public static void main(String[] args) throws Exception {
	Ftp f1 = new Ftp(args[0]);
	Ftp f = (Ftp) f1.clone();
	f.login();

	Enumeration e = f.nlst(args[1]);
	for (; e != null && e.hasMoreElements(); ) {
	    System.out.println(e.nextElement());
	}
	f.closeServer();
    }
}

