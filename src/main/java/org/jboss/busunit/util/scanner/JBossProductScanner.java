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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * 
 * @author Michael Pellegrini
 * 
 */
public class JBossProductScanner {

	private static final Pattern MF_VENDOR_PATTERN = Pattern.compile("jboss",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern MF_VERSION_ENT_PATTERN = Pattern.compile(
			"jbpapp", Pattern.CASE_INSENSITIVE);
	private static final Pattern MF_VERSION_COMU_PATTERN = Pattern.compile(
			"jboss", Pattern.CASE_INSENSITIVE);
	private static final Pattern MF_VERSION_ENTSOA_PATTERN = Pattern.compile(
			"soa", Pattern.CASE_INSENSITIVE);
	private static final Pattern MF_TITLE_EAP_PATTERN = Pattern.compile("eap",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern MF_TITLE_EWP_PATTERN = Pattern.compile("ewp",
			Pattern.CASE_INSENSITIVE);

	public static String startScan(File baseDir) throws Exception {
		if (baseDir == null) {
			throw new IllegalArgumentException("baseDir was not provided.");
		}

		if (!baseDir.exists()) {
			throw new IllegalArgumentException("baseDir does not exisit.");
		}

		System.out.println("JBoss Product Scanner started scanning from "
				+ baseDir);

		// List<File> filesFound = new ArrayList<File>();
		List filesFound = new ArrayList();
		scan(filesFound, baseDir);
		String report = produceReport(filesFound, baseDir);

		return report;

	}

	private static String produceReport(List filesFound, File baseDir) {
		final String NEW_LINE = System.getProperty("line.separator");

		String hostName = "Unknown";
		String ip = "Unknown";

		try {
			InetAddress inetAddr = InetAddress.getLocalHost();
			hostName = inetAddr.getHostName();
			ip = inetAddr.getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		StringBuffer sb = new StringBuffer();
		sb.append("JBoss Product Scanner Report").append(NEW_LINE);
		sb.append("Report created on : " + new java.util.Date()).append(
				NEW_LINE);
		sb.append("Started scan from ").append(baseDir).append(NEW_LINE)
				.append(NEW_LINE);
		sb.append("Summary").append(NEW_LINE);
		sb.append("-------------------------------------------------").append(
				NEW_LINE);
		sb.append("Host Name: ").append(hostName).append(NEW_LINE);
		sb.append("IP Address: ").append(ip).append(NEW_LINE);
		sb.append("Operating System: ").append(System.getProperty("os.name"));
		sb.append(" ").append(System.getProperty("os.version"))
				.append(NEW_LINE);
		sb.append("Operating System Architecture: ")
				.append(System.getProperty("os.arch")).append(NEW_LINE);
		sb.append("Number of available processors: ")
				.append(Runtime.getRuntime().availableProcessors())
				.append(NEW_LINE);
		sb.append("Java Virtual Machine: ")
				.append(System.getProperty("java.vendor")).append(" ");
		sb.append(System.getProperty("java.version")).append(NEW_LINE);
		sb.append("Found ").append(filesFound.size())
				.append(" possible JBoss ");
		sb.append((filesFound.size() == 1) ? "installation" : "installations")
				.append(NEW_LINE);
		sb.append(NEW_LINE);
		sb.append("Note: Identified Enterprise Platform installations could be ");
		sb.append("bundled with other JBoss products or other 3rd party applications and ");
		sb.append("therefore may not be a true standalone installation.");
		sb.append(NEW_LINE);
		sb.append("-------------------------------------------------")
				.append(NEW_LINE).append(NEW_LINE);

		for (Iterator iterator = filesFound.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();

			sb.append("Installation Location: ").append(file.toString())
					.append(NEW_LINE);
			try {
				JarFile jarFile = new JarFile(file);
				Manifest mf = jarFile.getManifest();

				String vendor = "unknown";
				Attributes mainAttrbs = null;
				if (mf != null) {
					mainAttrbs = mf.getMainAttributes();
					vendor = mainAttrbs.getValue("Implementation-Vendor");
				}

				if (vendor != null && MF_VENDOR_PATTERN.matcher(vendor).find()) {
					String title = mainAttrbs.getValue("Implementation-Title");
					String version = mainAttrbs
							.getValue("Implementation-Version");

					sb.append("Product Name: ").append(title).append(NEW_LINE);
					sb.append("Product Version: ").append(version)
							.append(NEW_LINE);

					if (version != null
							&& MF_VERSION_ENT_PATTERN.matcher(version).find()) {
						if (title != null
								&& MF_TITLE_EAP_PATTERN.matcher(title).find()) {
							// Enterprise Application Platform
							sb.append("This appears to be a JBoss Enterprise Application Platform installation");
						} else if (title != null
								&& MF_TITLE_EWP_PATTERN.matcher(title).find()) {
							// Enterprise Web Platform
							sb.append("This appears to be a JBoss Enterprise Web Platform installation");
						}
					} else if (version != null
							&& MF_VERSION_COMU_PATTERN.matcher(version).find()) {
						// Community
						sb.append("This appears to be a JBoss Application Server Community installation");
					} else if (version != null
							&& MF_VERSION_ENTSOA_PATTERN.matcher(version)
									.find()) {
						// Enterprise SOA Platform
						sb.append("This appears to be a JBoss SOA Platform Enterprise installation");
					} else {
						sb.append("This appears to be a valid JBoss installation but can't determine if this is enterprise or community");
					}
				} else {
					sb.append("This does not appear to be a valid JBoss installation");
				}
				sb.append(NEW_LINE).append(NEW_LINE);
				sb.append("-------------------------------------------------")
						.append(NEW_LINE).append(NEW_LINE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private static void scan(List results, File directory) {
		// Determine if there is sufficient rights to read the file or directory
		if (directory.canRead()) {
			File files[] = directory.listFiles();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];

					if (file.isDirectory() && !isLink(file)) {
						scan(results, file);
					} else {
						if (file.getName().equals("run.jar")) {
							System.out
									.println("Found possible JBoss installation at "
											+ file);
							results.add(file);
						}
					}
				}
			}
		} else {
			System.out.println("Permission Denied: Can not read " + directory);
		}
	}

	/**
	 * Determine if the file parameter is actually a link to the real file
	 * 
	 * @param file
	 * @return true if the file is a link, otherwise returns false
	 */
	private static boolean isLink(File file) {
		try {
			if (!file.exists()) {
				return true;
			} else {
				String cnnpath = file.getCanonicalPath();
				String abspath = file.getAbsolutePath();

				return !abspath.equals(cnnpath);
			}
		} catch (IOException ex) {
			return true;
		}
	}
}
