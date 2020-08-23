/**
 *  LaundryMonitor3
 *
 *  Copyright 2020 Eric Bouwkamp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "LaundryMonitor",
    namespace: "ericbouwkamp",
    author: "Eric Bouwkamp",
    description: "Laundry Monitor",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
    	input ("meter", "capability.powerMeter", title: "outlet", required: true, multiple: false, description: null)
        input(name: "wattageOn", type: "number", title: "wattage On Value", required: true, description: "Watts")
        input(name: "wattageOff", type: "number", title: "wattage Off Value", required: true, description: "Watts")
        input(name: "pushNotification", type: "bool", title: "Send a push notification", description: null, defaultValue: true)

	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    log.debug "Meter Name = ${meter.displayName}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "power", meterHandler)
}

def unsubscribe()
{
}

// TODO: implement event handlers
def meterHandler(evt) 
{
	def powerValue = evt.value as double
	def wattageOnValue = wattageOn as int
	def wattageOffValue = wattageOff as int
	
    if ((powerValue > wattageOnValue) && !state.powerMemory)
	{
		state.powerMemory = true
        log.debug("Power Memory True")
	}

	if ((powerValue <= wattageOffValue) && state.powerMemory && !state.notificationScheduled)
	{
    	runIn(60*5,sendCompletedNotification)
		log.debug "${meter.displayName} Notification Scheduled"
        state.notificationScheduled = true
		state.powerMemory = false
	}
    
    if ((powerValue > wattageOffValue) && state.notificationScheduled)
    {
    	unschedule()
		log.debug "${meter.displayName} Notification Unscheduled"
        state.notificationScheduled = false
		state.powerMemory = true
    }
}

def sendCompletedNotification() {
    if (pushNotification)
    {
        sendPush("${meter.displayName} Cycle Completed")
    }
    log.debug("${meter.displayName} Cycle Completed")
}