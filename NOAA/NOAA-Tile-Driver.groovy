/**
 *  ****************  NOAA Tile Driver  ****************
 *
 *  importUrl: https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/NOAA-Tile-Driver.groovy
 *
 *  Copyright 2019 Aaron Ward
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 * Last Update: 9/13/2020
 */

metadata {
	definition (
		name: "NOAA Tile",
		namespace: "aaronward",
		author: "Aaron Ward",
		importUrl: "https://raw.githubusercontent.com/imnotbob/Hubitat-4/master/NOAA/NOAA-Tile-Driver.groovy") {
		command "sendNoaaTile", ["string"]
		command "initialize"
		command "refreshTile"
		capability "Actuator"
		capability "Refresh"
		attribute "Alerts", "string"
		}

	preferences() {		
		input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
	}
}

def initialize() {
	log.info "NOAA Tile Driver Initializing."
	refresh()
	refreshTile()
}

def updated() {
	refresh()
}

def installed(){
	log.info "NOAA Tile has been Installed."
	sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true)
}

def refresh() {
	if(logEnable) runIn(900,logsOff)
}

void logsOff(){
	log.warn "Debug logging disabled."
	device.updateSetting("logEnable",[value:"false",type:"bool"])
}


void refreshTile() {
		if(logEnable) log.info "Requesting current weather alert from NOAA App."
		List noaaData = []
		try { noaaData = (List)parent.getTile() }
		catch (e) {}
		if(!noaaData) { 
			sendEvent(name: "Alerts", value: "No weather alerts to report.", displayed: true) 
			//runIn(60, refreshTile)
		}
		else {
			if(logEnable) log.info "Received the from NOAA Alerts: ${noaaData}"
			if(logEnable) log.info "Displaying ${noaaData.size} alerts on the dashboard."
			List fullmsg = []
			for(x=0;x<noaaData.size();x++) {
				m = noaaData[x].alertmsg =~ /(.|[\r\n]){1,378}\W/
				fullmsg = []
				while (m.find()) {
					fullmsg << m.group()
				}
				for(i=0;i<fullmsg.size();i++) {
					String noaaTile
					noaaTile = "<table style='position:relative;top:-10px;left:10px;border-collapse:collapse;width:97%'><tr style='border-bottom:medium solid #FFFFFF;'><td valign='bottom' style='text-align:left;width:50%;border-bottom:medium solid #FFFFFF;height:13px'><font style='font-size:12px;'>"
					noaaTile += "Alert: ${x+1}/${noaaData.size()}"
					noaaTile += "</font></td><td valign='bottom' style='text-align:right;border-bottom:medium solid #FFFFFF;width:50%;height:13px'><font style='font-size:12px;'>Page: ${i+1}/${fullmsg.size()}</font></td></tr>"
					noaaTile += "<tr><td colspan=2 valign='top' style='line-height:normal;width:90%;height:100px;text-align:left;border-top-style:none;border-top-width:medium;'><font style='font-size:13px'>${fullmsg[i]}"
					noaaTile += "</font></td></tr></table>"
					sendEvent(name: "Alerts", value: noaaTile, displayed: true)	  
					pauseExecution(8000)
				}
			}
			runIn(15, refreshTile)
		}
}
