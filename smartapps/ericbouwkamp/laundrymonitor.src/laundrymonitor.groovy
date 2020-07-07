/**
 *  LaundryMonitor
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
    description: "Enable debounced notifications for laundry completion",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "wattageOn", type: "number", title: "wattage On Value", required: true, description: "Watts")
        input(name: "wattageOff", type: "number", title: "wattage Off Value", required: true, description: "Watts")
        input(name: "delayTime", type: "number", title: "Debounce Time", required: true, description: "Seconds")
	}
    section {
        input("recipients", "contact", title: "Send notifications to") {
        input(name: "pushNotification", type: "bool", title: "Send a push notification", description: null, defaultValue: true)
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "lastCheckin", meterHandler)
}

def meterHandler(evt) 
{
	def powerValue = evt.value as double
	def wattageOnValue = wattageOn as int
	def wattageOffValue = wattageOff as int
	
	if ((powerValue > wattageOnValue) && !state.powerMemory)
	{
		sendNotification("Washer Running")
		state.powerMemory = t
	}

	if ((powerValue <= wattageOffValue) && state.powerMemory)
	{
		sendNotification("Washer Finished")
		state.powerMemory = f
	}
}


def sendNotification(notification) {
    if (pushNotification)
    {
        sendPush(notification)
        log.debug(notification)
    }
}