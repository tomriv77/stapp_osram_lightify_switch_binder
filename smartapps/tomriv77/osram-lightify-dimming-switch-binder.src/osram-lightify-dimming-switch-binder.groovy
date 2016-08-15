/**
 *  OSRAM Lightify Dimming Switch Binder
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
    name: "OSRAM Lightify Dimming Switch Binder",
    namespace: "tomriv77",
    author: "Tom Rivera",
    description: "Use to bind a switch any switch or outlet to a OSRAM Lightify Switch",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/tomriv77/stapp_osram_lightify_switch_binder/master/lightify-icon%401x.jpg",
    iconX2Url: "https://raw.githubusercontent.com/tomriv77/stapp_osram_lightify_switch_binder/master/lightify-icon%402x.jpg",
    iconX3Url: "https://raw.githubusercontent.com/tomriv77/stapp_osram_lightify_switch_binder/master/lightify-icon%403x.jpg"
)


preferences {
	page (name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", install: true, uninstall: true) {
        section("Select OSRAM Lightify Dimming Switch..."){
            input(name: "osramSwitch", type: "capability.button", title: "Which switch?", required: true)
        }
        section("Select device to turn on/off") {
            input(name: "target1", type: "capability.switch", title: "Which Target?", multiple: false, required: true)
            input(name: "isDimmable", title: "Is Dimmable Device?", type: "bool", required: false, defaultValue: true, submitOnChange: true)
            input(name: "powerOffTimer", title: "Use Power Off Delay Timer?", type: "bool", required: false, defaultValue: false, submitOnChange: true)
        }
        
        if(powerOffTimer !=  null && powerOffTimer) {
        	section("Time to wait before turning off device? (1 - 120 minutes)"){
                input(name: "offTimeInMin", type: "number", range: "1..120", title: "Time in minutes", required: true, default: "15")
            }
        }

        if(isDimmable == null || isDimmable) {
            section("Set number of light levels to step through (default 3)"){
                input(name: "levels", type: "number", range: "3..10", title: "Level count?", required: true, default: "3")
            }
            section("Select light range") {
            	 input(name: "maxLevel", type: "number", range: "50..99", title: "Max light level?", required: true, default: "99")
                 input(name: "minLevel", type: "number", range: "1..50", title: "Min light level?", required: true, default: "10")
            }
        } else {
            section {
                input(name: "controlSecondDevice", title: "Control Second Device (Hold Buttons)?", type: "bool", required: false, defaultValue: false, submitOnChange: true)
            }

            if(controlSecondDevice != null && controlSecondDevice) {
                section ("Secondary device to toggle on/off (press and hold)") {
                    input(name: "target2", type: "capability.switch", title: "Which Target?", multiple: false, required: true)
                }
            }
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
  subscribe(osramSwitch, "button.pushed", buttonPushedHandler)
  subscribe(osramSwitch, "button.held", buttonHeldHandler)
  subscribe(target1, "switch", switchStateChangeHandler)
}

def switchStateChangeHandler(evt) {
	log.debug "New event ${evt.name}, value ${evt.value}"
	if(evt.name == "switch") {
    	if(evt.value == "off") {
        	unschedule()
        } else if(evt.value == "on") {
        	unschedule()
        	runIn(offTimeInMin * 60, turnOffDevice, [overwrite: false])
        }
    }
}


def turnOffDevice() {
	log.debug "turnOffDevice() called"
	target1.off()
}

def buttonPushedHandler(evt) {
  def buttonNumber = parseJson(evt.data)?.buttonNumber
  if (buttonNumber==1) {
    log.debug "Button 1 pushed"
    target1.on()
  } else {
    log.debug "Button 2 pushed"
    target1.off()
  }
}

def buttonHeldHandler(evt) {
    log.debug "buttonHeldHandler invoked with ${evt.data}"
    def buttonNumber = parseJson(evt.data)?.buttonNumber

    if(isDimmable == null || isDimmable) {
    	def currLevel = target1.currentState("level").value as int
        def levelChange = 100 / (levels - 1)
        if (buttonNumber == 1) {
        	def upLevel = currLevel + levelChange
            log.debug "Button 1 held (currLevel is $currLevel, increment brightness by $levelChange)"
            if(upLevel > maxLevel) upLevel = maxLevel
            target1.setLevel(upLevel)
        } else {
        	def downLevel = currLevel - levelChange
            log.debug "Button 2 held (currLevel is $currLevel, decrement brightness by $levelChange)"
            if(downLevel < minLevel) downLevel = minLevel
            target1.setLevel(downLevel)
        }

  	} else if (controlSecondDevice != null && controlSecondDevice && target2 != null) {
 		if (buttonNumber == 1) {
            log.debug "Button 1 held"
            target2.on()
      	} else {
          	log.debug "Button 2 held"
          	target2.off()
      	}
  	}
}