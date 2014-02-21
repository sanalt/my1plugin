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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.digimarc.DMSDemo.R;
import com.digimarc.dms.resolver.ResolveResult;

public class DMSListviewAdapter
	extends
	ArrayAdapter<DMSItemData>
{
	private Context mContext = null;
	private List<DMSItemData> mItems = null;

	public DMSListviewAdapter(
		Context context,
		List<DMSItemData> items)
	{
		super(
			context,
			R.layout.listview_item,
			items);
		this.mContext = context;
		this.mItems = items;
	}

	@Override
	public
		View
		getView(
			int position,
			View convertView,
			ViewGroup parent)
	{
		View rowView = null;
		try
		{
			if ((parent != null) && (mContext != null))
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(
					R.layout.listview_item,
					parent,
					false);
				if ((mItems != null) && (mItems.size() > 0) && (position >= 0) && (position < mItems.size()))
				{
					DMSItemData data = mItems.get(position);
					if (data != null)
					{
						TextView textView = (TextView) rowView.findViewById(R.id.textview);
						ImageView imageView = (ImageView) rowView.findViewById(R.id.imageview);
						ListView listView = (ListView) parent;
						if ((textView != null) && (imageView != null))
						{
							textView.setText(data.mDescription);

							int imageID = -1;
							if (imageID == -1)
							{
								if ((data.mOrigin != null) && (data.mOrigin.isEmpty() == false))
								{
									String temp = data.mOrigin.toLowerCase();
									if (temp.startsWith("tds") == true)
									{
										imageID = R.drawable.audio_icon;
									}
									else if (temp.startsWith("qrcode") == true)
									{
										imageID = R.drawable.qrcode_icon;
									}
									else if (temp.startsWith("barcode") == true)
									{
										imageID = R.drawable.barcode_icon;
									}
								}
							}

							if (imageID == -1)
							{
								if (data.mBitmap != null)
								{
									imageView.setImageBitmap(data.mBitmap);
								}
								else
								{
									if (data.mValue != null)
									{
										if (data.mValue instanceof ResolveResult)
										{
											imageID = R.drawable.digimarc_logo;
										}
									}
								}
							}

							if (imageID != -1)
							{
								imageView.setImageResource(imageID);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"ListviewAdapter.getView",
				e);
		}
		return (rowView);
	}
}