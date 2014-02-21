/***************************************************************************************************
 
 The technology detailed in this software is the subject of various pending and issued patents,
 both internationally and in the United States, including one or more of the following patents:
 
 5,636,292 C1; 5,710,834; 5,832,119 C1; 6,286,036; 6,311,214; 6,353,672; 6,381,341; 6,400,827;
 6,516,079; 6,580,808; 6,614,914; 6,647,128; 6,681,029; 6,700,990; 6,704,869; 6,813,366;
 6,879,701; 6,988,202; 7,003,132; 7,013,021; 7,054,465; 7,068,811; 7,068,812; 7,072,487;
 7,116,781; 7,158,654; 7,280,672; 7,349,552; 7,369,678; 7,461,136; 7,564,992; 7,567,686;
 7,590,259; 7,657,057; 7,672,477; 7,720,249; 7,751,588; and EP 1137251 B1; EP 0824821 B1;
 and JP-3949679, all owned by Digimarc Corporation.
 
 Use of such technology requires a license from Digimarc Corporation, USA.  Receipt of this software
 conveys no license under the foregoing patents, nor under any of Digimarc’s other patent, trademark,
 or copyright rights.
 
 This software comprises CONFIDENTIAL INFORMATION, including TRADE SECRETS, of Digimarc Corporation,
 USA, and is protected by a license agreement and/or non-disclosure agreement with Digimarc.  It is
 important that this software be used, copied and/or disclosed only in accordance with such
 agreements.
 
 © Copyright, Digimarc Corporation, USA.  All Rights Reserved.
 
***************************************************************************************************/package com.digimarc.DMSUtils;

public class DMSConstants
{
	public static final int ONE_K = 1024;
	public static final int ONE_MEG = (ONE_K * 1024);
	public static final int ONE_GIG = (ONE_MEG * 1024);
	
	public static String ROOT_SUBFOLDER = "DigimarcRootSubfolder";
	public static String SETTINGS_FILENAME = "DigimarcSettings.txt";
	public static String DEBUG_LOGS_SUBFOLDER = "DebugLogFiles";
	public static String IMAGES_SUBFOLDER = "Images";
	public static String DEBUG_LOG_EXTENSION = "txt";
	
	public static final String CRLF = "\r\n";
	public static final String EQUAL_SIGN = "=";
	
	public static final int ONE_SECOND_MILLISECS = 1000;
	public static final int ONE_MINUTE_MILLISECS = (ONE_SECOND_MILLISECS * 60);
	public static final int ONE_HOUR_MILLISECS = (ONE_MINUTE_MILLISECS * 60);
	public static final int ONE_DAY_MILLISECS = (ONE_HOUR_MILLISECS * 24);
	public static final int DEFAULT_TIMEOUT = (ONE_SECOND_MILLISECS * 10);
	
	public static final int EXCEPTION_LOG_LEVEL = 9999;
}
