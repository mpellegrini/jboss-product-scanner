/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2010-2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more 
 * details. You should have received a copy of the GNU Lesser General Public 
 * License, v.2.1 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
 * 02110-1301, USA.
 */

package org.jboss.busunit.util.scanner;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;

/**
 * 
 * @author Michael Pellegrini
 *
 */
public class Main {
	private static final String REPORT_FILE_PREFIX = "jboss-prod-scanner_";
	
	public static void main(String[] args) throws Exception {
		String dirName = "/";
		if (args.length > 0) {
			dirName = args[0];
		}
		
		File baseDir = new File(dirName);	
		String report = JBossProductScanner.startScan(baseDir);
		
		// Write report to file system
		PrintWriter out = null;
		try {
			InetAddress inetAddr = InetAddress.getLocalHost();
			String hostName = inetAddr.getHostName();
			
			File file = new File(REPORT_FILE_PREFIX + hostName + ".txt");
			 
			int counter = 1;
			while (file.exists()) {
				// Report with the same name already exists. Make the report
				// file name unique by appending a unique number to the name.
				file = new File(REPORT_FILE_PREFIX + hostName + "-" + counter++ + ".txt");				
			}
			
			out = new PrintWriter(new FileWriter(file));
			out.write(report);
			System.out.println();
			System.out.println("Scan completed: results saved to " + REPORT_FILE_PREFIX + hostName + ".txt");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("JBoss Product Scanner report could not be created", e);
		} finally {
			out.close();
		}
	}

}
