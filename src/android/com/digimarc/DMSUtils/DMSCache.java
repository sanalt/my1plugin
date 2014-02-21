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
 conveys no license under the foregoing patents, nor under any of Digimarc�s other patent, trademark,
 or copyright rights.
 
 This software comprises CONFIDENTIAL INFORMATION, including TRADE SECRETS, of Digimarc Corporation,
 USA, and is protected by a license agreement and/or non-disclosure agreement with Digimarc.  It is
 important that this software be used, copied and/or disclosed only in accordance with such
 agreements.
 
 � Copyright, Digimarc Corporation, USA.  All Rights Reserved.
 
***************************************************************************************************/

package com.digimarc.DMSUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * General purpose LRU cache. 
 *
 * @param <K>  key type
 * @param <V>  value type
 */
public class DMSCache<K,V> {
	final LinkedHashMap<K, V> MRUdata;
	final Map<K,V> LRUdata;
	
	public DMSCache(final int capacity)
	{
	    LRUdata = new WeakHashMap<K, V>();
	
	    MRUdata = new LinkedHashMap<K, V>(capacity+1, 1.0f, true) {
	    	protected boolean removeEldestEntry(Map.Entry<K,V> entry)
	        {
	            if (this.size() > capacity) {
	                LRUdata.put(entry.getKey(), entry.getValue());
	                return true;
	            }
	            return false;
	        };
	    };
	}
	
	public synchronized V get(K key)
	{
	    V value = MRUdata.get(key);
	    if (value!=null)
	        return value;
	    value = LRUdata.get(key);
	    if (value!=null) {
	        LRUdata.remove(key);
	        MRUdata.put(key, value);
	    }
	    return value;
	}
	
	public synchronized void set(K key, V value)
	{
	    LRUdata.remove(key);
	    MRUdata.put(key, value);
	}
}