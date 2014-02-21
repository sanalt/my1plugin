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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.digimarc.DMSDemo.R;

public class DMSErrorMsgDialog
	extends
	DialogFragment
	implements
	OnClickListener
{
	public enum TYPE
	{
		ERROR,
		WARNING,
		INFO,
		QUESTION,
	};

	public String mTitle = "";
	public String mParagraph = "";
	public TYPE mType = TYPE.ERROR;

	public DMSErrorMsgDialog()
	{
	}

	@Override
	public
		Dialog
		onCreateDialog(
			Bundle savedInstanceState)
	{
		Dialog dialog = null;
		try
		{
			if ((mTitle != null) && (mTitle.isEmpty() == false) && (mParagraph != null) && (mParagraph.isEmpty() == false))
			{
				Activity activity = getActivity();
				if (activity != null)
				{
					LayoutInflater inflater = activity.getLayoutInflater();
					if (inflater != null)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
						builder.setTitle(mTitle);
						builder.setPositiveButton(
							"OK",
							this);

						View view = inflater.inflate(
							R.layout.error_msg_layout,
							null);
						if (view != null)
						{
							TextView textView = (TextView) view.findViewById(R.id.errorMsgParagraph);
							if (textView != null)
							{
								textView.setText(mParagraph);
							}

							ImageView imageView = (ImageView) view.findViewById(R.id.errorIcon);
							if (imageView != null)
							{
								int imageID = 0;
								switch (mType)
								{
									case ERROR:
										default:
										imageID = R.drawable.error_icon;
										break;
										
									case WARNING:
										imageID = R.drawable.warning_icon;
										break;
										
									case INFO:
										imageID = R.drawable.info_icon;
										break;
										
									case QUESTION:
										imageID = R.drawable.question_icon;
										break;
								}
								imageView.setImageResource(imageID);
							}

							builder.setView(view);

							dialog = builder.create();

							dialog.show();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			DMSDebugLog.Write(
				"ErrorMsgDialog.onCreateDialog",
				e);
		}
		return (dialog);
	}

	@Override
	public
		void
		onClick(
			DialogInterface dialog,
			int which)
	{
		switch (which)
		{
			case AlertDialog.BUTTON_POSITIVE:
				this.dismiss();
				break;
		}
	}
}
