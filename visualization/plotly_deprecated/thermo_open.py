#!/usr/bin/python

# Copyright (C) 2014 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Visualization for Temperature Data Trends.

Usage:
0. Update config.txt file with your actual information.

1. Run this file by typing the following at your command prompt:
python thermo_open.py 8001

2. Install the Temperature Data Trends Chrome extension.

3. Navigate to the view-only version of the responses page in Chrome. The
URL for this page should end with "/pubhtml". You can get to this page by going
to the editable version of the Spreadsheet and doing File > Publish to the web.

The Chrome extension will automatically refresh the page and post data to
this python script which will generate the charts through Plot.ly.

Please make sure you are using your own Plot.ly API key.
"""

import SimpleHTTPServer
import SocketServer
import logging
import cgi
import pandas as pd
import sys
import csv
import string
import plotly.plotly as py
from plotly.graph_objs import *
import os
import time
from datetime import datetime, date, timedelta
import smtplib




if len(sys.argv) > 2:
    PORT = int(sys.argv[2])
    I = sys.argv[1]
elif len(sys.argv) > 1:
    PORT = int(sys.argv[1])
    I = ""
else:
    PORT = 8001
    I = ""


class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):

    def do_GET(self):
        logging.warning("======= GET STARTED =======")
        logging.warning(self.headers)
        SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)

    def do_POST(self):
        logging.warning("======= POST STARTED =======")
        logging.warning(self.headers)
        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={'REQUEST_METHOD':'POST',
                     'CONTENT_TYPE':self.headers['Content-Type'],
                     })
        logging.warning("======= POST VALUES =======")
	start_time = time.time()
	y = str(form)
	x = y.find("</summary>")
	z = y.find("lastStatus\\n")
	table = y[z+5:x]
	data = y[x+16:]
	data = data.split(',')
	table = table.split(',')
	timestamp = []
	user = []
	temp = []
	status = []
	pid = []
	last_entry = []
	last_temp = []
	max_temp = []
	last_status = []
	a1 = 0
	b1 = 1
	c1 = 2
	d1 = 3
	e1 = 4
	a = 8
	b = 9
	c = 10
	d = 11
	df = pd.DataFrame()
	df2 = pd.DataFrame()
	df3 = pd.DataFrame()
	df4 = pd.DataFrame()
	while e1 <= len(table):
		pid.append(table[a1:a1+1])
		last_entry.append(table[b1:b1+1])
		last_temp.append(table[c1:c1+1])
		max_temp.append(table[d1:d1+1])
		last_status.append(table[e1:e1+1])
		a1 = a1+4
		b1 = b1+4
		c1 = c1+4
		d1 = d1+4
		e1 = e1+4
	df3["user"] = pid
	df3["last_entry"] = last_entry
	df3["last_temp"] = last_temp
	df3["max_temp"] = max_temp
	df3["last_status"] = last_status
	df3.to_csv("table.csv")
	while d <= len(data):
		timestamp.append(data[a:a+1])
		user.append(data[b:b+1])
		temp.append(data[c:c+1])
		status.append(data[d:d+1])
		a = a+4
		b = b+4
		c = c+4
		d = d+4
	df2["timestamp"] = timestamp
	df2["user"] = user
	df2["temp"] = temp
	df2["status"] = status
	df2.to_csv("data.csv")
	
	wd = os.getcwd()
	df1 = pd.read_csv(wd+"/data.csv")
	df5 = pd.read_csv(wd+"/table.csv")
	timestamp1 = []
	user1 = []
	temp1 = []
	status1 = []
	pid1 = []
	last_entry1 = []
	last_temp1 = []
	max_temp1 = []
	last_status1 = []
	for i in df5["user"]:
		pid1.append(i[10:-2])
	for i in df5["last_entry"]:
		last_entry1.append(i[3:-2])
	for i in df5["last_temp"]:
		last_temp1.append(i[3:-2])
	for i in df5["max_temp"]:
		max_temp1.append(i[3:-2])
	for i in df5["last_status"]:
		l = i.find("\\n")
		last_status1.append(i[3:l-1])
	df4["user"] = pid1
	df4["last_entry"] = last_entry1
	df4["last_temp"] = last_temp1
	df4["max_temp"] = max_temp1
	df4["last_status"] = last_status1
	df4.to_csv("table.csv")

	for k in df1["timestamp"]:
		timestamp1.append(k[6:-2])
	for k in df1["user"]:
		user1.append(k[3:-2])
	for k in df1["temp"]:
		temp1.append(k[3:-2])
	for k in df1["status"]:
		status1.append(k[3:-2])
	df["timestamp"] = timestamp1
	df["user"] = user1
	df["temp"] = temp1
	df["status"] = status1
	df.to_csv("data.csv")
	newt = []
	unix_time = [] 

		
	for p in df["timestamp"]:
		p2 = p.find(" ")
		p3 = p.find("/")
		p4 = p.find("201")
		p5 = p.find(":")
		a1 = p[:p3]	
		a2 = p[p3+1:p4-1]
		a3 = p[p4:p2]
		a4 = p[p2+1:p5]
		a5 = p[p5+1:p5+3]
		a6 = p[p5+4:]
	
		if len(a2) == 1:
			a2 = "0"+a2
		if len(a4) == 1:
			a4 = "0"+a4
		d = datetime(int(a3), int(a1), int(a2), int(float(a4)), int(float(a5)), int(float(a6)))
		unix = time.mktime(d.timetuple())
		newt.append(d)
		unix_time.append(unix)
	time_now = time.strftime("%Y-%m-%d %H:%M:%S")
	print time_now
	time1 = []
	df['timestamp2'] = newt
	df['unix_time'] = unix_time
	a = []
	with open(wd+"/config.txt") as f:
   		for i in f:
       			a.append(i.strip())
    	f.close()
	username = a[0][a[0].find("=")+1:].strip()
	api_key = a[1][a[1].find("=")+1:].strip()
	max_temp_flag = float(str(a[2][a[2].find("=")+1:]).strip())
	last_temp_flag = int(str(a[3][a[3].find("=")+1:]).strip())
	senders_email = a[4][a[4].find("=")+1:].strip()
	senders_password = a[5][a[5].find("=")+1:].strip()
	recievers_email = a[6][a[6].find("=")+1:].strip()
	
	from_addr = senders_email
	to_addr_list = [recievers_email]
	login = senders_email
	password = senders_password

	ids = df["user"]
	ids1 = list(set(ids))
	ids1 = sorted(ids1)
	print len(ids)
	print len(ids1)
	stan = [98.6,98.6]
	fever = [max_temp_flag,max_temp_flag]
	timestamp3 = []
	numtime = []
	g = 0
	h = 1
	for p in df5["last_entry"]:
		p2 = p.find(" ")
		p3 = p.find("/")
		p4 = p.find("201")
		p5 = p.find(":")
		a1 = p[2:p3]	
		a2 = p[p3+1:p4-1]
		a3 = p[p4:p4+4]
		a4 = p[p4+5:p5]
		a5 = p[p5+1:p5+3]
		a6 = p[p5+4:-2]			
			

		if len(a2) == 1:
			a2 = "0"+a2
		if len(a4) == 1:
			a4 = "0"+a4

		num1 = datetime(int(a3), int(a1), int(a2), int(a4), int(a5), int(a6))
		time_flag = datetime.now() - timedelta(hours=last_temp_flag) 

		if num1 <= time_flag:
			numtime.append("No")
		else:
			numtime.append("Yes")
	df4["24hrs"] = numtime
	
	def sendemail(from_addr, to_addr_list,
			subject, message,
			login, password,
			smtpserver='smtp.gmail.com:587'):
		header  = 'From: %s\n' % from_addr
		header += 'To: %s\n' % ','.join(to_addr_list)
	# 	header += 'Cc: %s\n' % ','.join(cc_addr_list)
		header += 'Subject: %s\n\n' % subject
		message = header + message

		server = smtplib.SMTP(smtpserver)
		server.starttls()
		server.login(login,password)
		problems = server.sendmail(from_addr, to_addr_list, message)
		server.quit()
	
	
## start aggregate graph
	time_min1 = df["timestamp2"].min()
	time1.append(time_min1)
	time1.append(time_now)
	id3 = df.sort(["unix_time"], ascending=True)
	name = "All Users"
	## plotly credentials, which will change with new accounts (username and API key)
	py.sign_in(username,api_key)
	trace1 = Scatter(
		x = time1,
		y = fever,
		mode = 'lines',
		name = str(max_temp_flag),
		text = time1,
		marker=Marker(
			color='rgb(243,16,16)'
			),
		line=Line(
			dash='dash'
		),
	opacity = 0.8
	)
	trace2 = Scatter(
		x = time1,
		y = stan,
		mode = 'lines',
		name = '98.6',
		text = time1,
		marker=Marker(
			color='rgb(62,219,62)'
			),
		line=Line(
			dash='dash'
		),
	opacity = 0.8
	)
	data = Data([trace1,trace2])
	layout = Layout(
		title="All Users",
		titlefont=Font(
		family='',
		size=0,
		color=''
		),
		font=Font(
		family='"Open sans", verdana, arial, sans-serif',
		size=12,
		color='#444'
		),
		showlegend=True,
		autosize=True,
		width=1631,
		height=553,
		xaxis=XAxis(
		title='timestamp',
		titlefont=Font(
			family='',
			size=0,
			color=''
		),
		range=[time_min1, time_now],
		domain=[0, 1],
		type='date',
		rangemode='normal',
		autorange=False,
		showgrid=False,
		zeroline=True,
		showline=True,
		autotick=True,
		nticks=0,
		ticks='',
		showticklabels=True,
		tick0=0,
		dtick=1,
		ticklen=5,
		tickwidth=1,
		tickcolor='#444',
		tickangle='-45',
		tickfont=Font(
			family='',
			size=0,
			color=''
		),
		exponentformat='B',
		showexponent='all',
		mirror=False,
		gridcolor='#eee',
		gridwidth=1,
		zerolinecolor='#444',
		zerolinewidth=1,
		linecolor='#444',
		linewidth=1,
		anchor='y',
		overlaying=False,
		position=0
		),
		yaxis=YAxis(
		title='temp',
		titlefont=Font(
			family='',
			size=0,
			color=''
		),
		range=[92, 106],
		domain=[0, 1],
		type='linear',
		rangemode='normal',
		autorange=False,
		showgrid=False,
		zeroline=True,
		showline=False,
		autotick=True,
		nticks=0,
		ticks='',
		showticklabels=True,
		tick0=0,
		dtick=1,
		ticklen=5,
		tickwidth=1,
		tickcolor='#444',
		tickangle='auto',
		tickfont=Font(
			family='',
			size=0,
			color=''
		),
		exponentformat='B',
		showexponent='all',
		mirror=False,
		gridcolor='#eee',
		gridwidth=1,
		zerolinecolor='#444',
		zerolinewidth=1,
		linecolor='#444',
		linewidth=1,
		anchor='x',
		overlaying=False,
		position=0
		),
		legend=Legend(
		x=1.02,
		y=1,
		traceorder='normal',
		font=Font(
			family='',
			size=0,
			color=''
		),
	
		bgcolor='#fff',
		bordercolor='#444',
		borderwidth=0,
		xanchor='left',
		yanchor='top'
		),
		margin=Margin(
		l=80,
		r=80,
		b=110,
		t=100,
		pad=0,
		autoexpand=True
		),
		paper_bgcolor='#fff',
		plot_bgcolor='#fff',
		hovermode='x',
		dragmode='zoom',
		separators='.,',
		barmode='group',
		bargap=0.2,
		bargroupgap=0,
		boxmode='overlay',
		hidesources=False
	)
	fig = Figure(data=data, layout=layout)
	plot_url_main = py.plot(fig, filename=name, auto_open=False, world_readable=False, width=1500)
	for i in ids1:
		id4 = df[df["user"] == i]
		data.append(Scatter(
			x = id4['timestamp2'],
			y = id4['temp'],
			mode='lines+markers',
			name = i,
			text = id4['timestamp2']))
    
	layout = Layout(
		title="All Users",
		titlefont=Font(
		family='',
		size=0,
		color=''
		),
		font=Font(
		family='"Open sans", verdana, arial, sans-serif',
		size=12,
		color='#444'
		),
		showlegend=True,
		autosize=True,
		width=1631,
		height=553,
		xaxis=XAxis(
		title='timestamp',
		titlefont=Font(
			family='',
			size=0,
			color=''
		),
		range=[time_min1, time_now],
		domain=[0, 1],
		type='date',
		rangemode='normal',
		autorange=False,
		showgrid=False,
		zeroline=True,
		showline=True,
		autotick=True,
		nticks=0,
		ticks='',
		showticklabels=True,
		tick0=0,
		dtick=1,
		ticklen=5,
		tickwidth=1,
		tickcolor='#444',
		tickangle='-45',
		tickfont=Font(
			family='',
			size=0,
			color=''
		),
		exponentformat='B',
		showexponent='all',
		mirror=False,
		gridcolor='#eee',
		gridwidth=1,
		zerolinecolor='#444',
		zerolinewidth=1,
		linecolor='#444',
		linewidth=1,
		anchor='y',
		overlaying=False,
		position=0
		),
		yaxis=YAxis(
		title='temp',
		titlefont=Font(
			family='',
			size=0,
			color=''
		),
		range=[92, 106],
		domain=[0, 1],
		type='linear',
		rangemode='normal',
		autorange=False,
		showgrid=False,
		zeroline=True,
		showline=False,
		autotick=True,
		nticks=0,
		ticks='',
		showticklabels=True,
		tick0=0,
		dtick=1,
		ticklen=5,
		tickwidth=1,
		tickcolor='#444',
		tickangle='auto',
		tickfont=Font(
			family='',
			size=0,
			color=''
		),
		exponentformat='B',
		showexponent='all',
		mirror=False,
		gridcolor='#eee',
		gridwidth=1,
		zerolinecolor='#444',
		zerolinewidth=1,
		linecolor='#444',
		linewidth=1,
		anchor='x',
		overlaying=False,
		position=0
		),
		legend=Legend(
		x=1.02,
		y=1,
		traceorder='normal',
		font=Font(
			family='',
			size=0,
			color=''
		),

		bgcolor='#fff',
		bordercolor='#444',
		borderwidth=0,
		xanchor='left',
		yanchor='top'
		),
		margin=Margin(
		l=80,
		r=80,
		b=110,
		t=100,
		pad=0,
		autoexpand=True
		),
		paper_bgcolor='#fff',
		plot_bgcolor='#fff',
		hovermode='x',
		dragmode='zoom',
		separators='.,',
		barmode='group',
		bargap=0.2,
		bargroupgap=0,
		boxmode='overlay',
		hidesources=False
	)
	fig = Figure(data=data, layout=layout)
	plot_url_main = py.plot(data, filename=name, world_readable=False, auto_open=False)
	print "summary chart {}".format(plot_url_main)

### looping through personal graphs
	diff = []
	m = 2
	g = []
	l = []
	for i in df4["user"]:
		time1 = []
		id1 = df[df["user"] == i]
		id2 = id1.sort(["unix_time"], ascending=True)
		time_min = id2["timestamp2"].min()
		time_last = id2["timestamp2"].max()
		time1.append(time_min)
		time1.append(time_now)
		print time_last	
		print str(h)+" out of "+str(len(ids1))	
		h += 1
		print i	
		name = i+"temp_data"
		## plotly credentials, which will change with new accounts (username and API key)
		py.sign_in(username,api_key)
		i = str(i)
		trace1 = Scatter(
			x=id2["timestamp2"],
			y=id2['temp'],
			mode='lines+markers',
			name='temp',
			text = id2["timestamp2"],
		    )
		trace2 = Scatter(
			x = time1,
			y = fever,
			mode = 'lines',
			name = str(max_temp_flag),
			text = time1,
			marker=Marker(
				color='rgb(243,16,16)'
				),
			line=Line(
				dash='dash'
			),
		opacity = 0.8
		)
		trace3 = Scatter(
			x = time1,
			y = stan,
			mode = 'lines',
			name = '98.6',
			text = time1,
			marker=Marker(
				color='rgb(62,219,62)'
				),
			line=Line(
				dash='dash'
			),
		opacity = 0.8
		)
		trace4 = Bar(
    			x=id2["timestamp2"],
    			y=id2["status"],
   			name='Sick',
    			marker=Marker(
        		color='rgb(255, 0, 0)'
    		),
    		opacity=0.4,
    		yaxis='y2'
		)
		data = Data([trace1, trace2, trace3, trace4])
		layout = Layout(
		    title=i,
		    titlefont=Font(
			family='',
			size=0,
			color=''
		    ),
		    font=Font(
			family='"Open sans", verdana, arial, sans-serif',
			size=12,
			color='#444'
		    ),
		    showlegend=True,
		    autosize=True,
		    width=1631,
		    height=553,
		    xaxis=XAxis(
			title='timestamp',
			titlefont=Font(
			    family='',
			    size=0,
			    color=''
			),
			range=[0,0],
			domain=[0, 1],
			type='date',
			rangemode='normal',
			autorange=True,
			showgrid=False,
			zeroline=True,
			showline=True,
			autotick=True,
			nticks=0,
			ticks='',
			showticklabels=True,
			tick0=0,
			dtick=1,
			ticklen=5,
			tickwidth=1,
			tickcolor='#444',
			tickangle='-45',
			tickfont=Font(
			    family='',
			    size=0,
			    color=''
			),
			exponentformat='B',
			showexponent='all',
			mirror=False,
			gridcolor='#eee',
			gridwidth=1,
			zerolinecolor='#444',
			zerolinewidth=1,
			linecolor='#444',
			linewidth=1,
			anchor='y',
			overlaying=False,
			position=0
		    ),
		    yaxis=YAxis(
			title='temp',
			titlefont=Font(
			    family='',
			    size=0,
			    color=''
			),
			range=[92, 106],
			domain=[0, 1],
			type='linear',
			rangemode='normal',
			autorange=False,
			showgrid=False,
			zeroline=True,
			showline=False,
			autotick=True,
			nticks=0,
			ticks='',
			showticklabels=True,
			tick0=0,
			dtick=1,
			ticklen=5,
			tickwidth=1,
			tickcolor='#444',
			tickangle='auto',
			tickfont=Font(
			    family='',
			    size=0,
			    color=''
			),
			exponentformat='B',
			showexponent='all',
			mirror=False,
			gridcolor='#eee',
			gridwidth=1,
			zerolinecolor='#444',
			zerolinewidth=1,
			linecolor='#444',
			linewidth=1,
			anchor='x',
			overlaying=False,
			position=0
		    ),
		    legend=Legend(
			x=1.02,
			y=1,
			traceorder='normal',
			font=Font(
			    family='',
			    size=0,
			    color=''
			),
			   
			bgcolor='#fff',
			bordercolor='#444',
			borderwidth=0,
			xanchor='left',
			yanchor='top'
		    ),
		    margin=Margin(
			l=80,
			r=80,
			b=110,
			t=100,
			pad=0,
			autoexpand=True
		    ),
		    paper_bgcolor='#fff',
		    plot_bgcolor='#fff',
		    hovermode='x',
		    dragmode='zoom',
		    separators='.,',
		    barmode='group',
		    bargap=0.85,
		    bargroupgap=0.85,
		    boxmode='overlay',
		    hidesources=False,
		yaxis2=YAxis(
		title='',
		titlefont=Font(
		    family='',
		    size=0,
		    color=''
		),
		range=[0, 1],
		domain=[0, 1],
		type='linear',
		rangemode='normal',
		autorange=False,
		showgrid=False,
		zeroline=False,
		showline=False,
		autotick=True,
		nticks=0,
		ticks='',
		showticklabels=False,
		tick0=0,
		dtick=0.2,
		ticklen=5,
		tickwidth=1,
		tickcolor='#444',
		tickangle='auto',
		tickfont=Font(
		    family='',
		    size=0,
		    color=''
		),
		exponentformat='B',
		showexponent='all',
		mirror=False,
		gridcolor='#eee',
		gridwidth=1,
		zerolinecolor='#444',
		zerolinewidth=1,
		linecolor='#444',
		linewidth=1,
		anchor='x',
		overlaying='y',
		side='right',
		position=0
		)
		)
		fig = Figure(data=data, layout=layout)
		locals()["plot_url"+str(m)] = py.plot(fig, filename=name, auto_open=False, world_readable=False, width=1500)		
		g.append(locals()["plot_url"+str(m)])
		l.append(i)
		m +=1
	links = pd.DataFrame()
	links["ids"] = l
	links["links"] = g
	
	print "DO NOT REFRESH PAGE, re-writing .html file now."
	html_file = wd+"/dashboard.html"
	f = open(html_file,'w')
	f.write("""
	<html>
	<head>
	<meta http-equiv="refresh" content="1500"></meta>
	<a name="top"></a>
	</head>
	<p class = "sanserif""><TABLE cellpadding="4" style="border: 1px solid #000000; border-collapse: collapse;" border="1">
	<TR>
		<TH>#</TH>
		<TH>User ID</TH>
		<TH>Last Entry</TH>
		<TH>Last Temp</TH>
		<TH>Max Temp</TH>
		<TH>Last Status</TH>
		<TH>Temp Within """+str(last_temp_flag)+"""hrs</TH>
	</TR>
	""")
	f.close()
	x = 1
	y = 0
	with open(html_file, 'a') as f:
		df4 = df4.sort(["user"], ascending=True)
		for i in df4["max_temp"]:
			f.write("""
				<TR>
					<TD><b>"""+str(x)+"""</b></TD>
					<TD><a href="""+links["links"][y]+""".embed target="_blank">"""+df4["user"][y]+"""</a></TD>
					<TD>"""+df4["last_entry"][y]+"""</TD>
					<TD>"""+df4["last_temp"][y]+"""</TD>
				""")
			if float(df4["max_temp"][y]) >= max_temp_flag:
				f.write("""
					<TD bgcolor="red">"""+df4["max_temp"][y]+"""</TD>
					""")
			else:
				f.write("""
					<TD>"""+df4["max_temp"][y]+"""</TD>
				""")
				
			if df4["last_status"][y] == "Sick":
				f.write("""
					<TD bgcolor="red">"""+df4["last_status"][y]+"""</TD>
						""")
			else:
				f.write("""
					<TD>"""+df4["last_status"][y]+"""</TD>
				""")
				
			if df4["24hrs"][y] == "No":
				f.write("""
					<TD bgcolor="red">"""+df4["24hrs"][y]+"""</TD>
				</TR>
				""")
			else:
				f.write("""
					<TD>"""+df4["24hrs"][y]+"""</TD>
				</TR>
				""")
			x += 1
			y += 1
		f.write("""
			</TABLE></p>
		<p><iframe height="500" scrolling="no" seamless="seamless" src="""+plot_url_main+""" width="1100"></iframe></p>
		""")		
		y = 0
		f.write("""
		</html>
		""")
		f.close()
		print "Open dashboard file now"
		x = 1
		y = 0
		df4 = df4.sort(["user"], ascending=True)
		for i in df4["max_temp"]:
			if df4["24hrs"][y] == "No" or df4["last_status"][y] == "Sick" or float(df4["max_temp"][y]) >= max_temp_flag:
				subject = df4["user"][y]+" has been flagged." 
				message = " "
				if df4["24hrs"][y] == "No":
					message = message+"The user's last temperature was taken more than "+str(last_temp_flag)+" hours ago, at "+str(df4["last_entry"][y])+"."
				if df4["last_status"][y] == "Sick":
					message = message+" The user's status was just reported as "+df4["last_status"][y]+" at "+df4["last_entry"][y]+"."
				if float(df4["max_temp"][y]) >= max_temp_flag:
					message = message+" The user's max temperature is = "+str(float(df4["max_temp"][y]))+"."
				
				sendemail(from_addr, to_addr_list, subject, message, login, password)
			print "sending emails ..."
			x += 1
			y += 1
        logging.warning("\n")
        SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)
	end_time = time.time()
	print end_time-start_time
	time.sleep(5)

Handler = ServerHandler

httpd = SocketServer.TCPServer(("", PORT), Handler)

print "Serving at: http://%(interface)s:%(port)s" % dict(interface=I or "localhost", port=PORT)
httpd.serve_forever()
