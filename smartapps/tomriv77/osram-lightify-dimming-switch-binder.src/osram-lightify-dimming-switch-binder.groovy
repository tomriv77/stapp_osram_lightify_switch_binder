/**
 *  OSRAM Lightify Dimming Switch Binder
 *
 *  Copyright 2016 Michael Hudson
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
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page (name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", install: true, uninstall: true) {
        section("Select OSRAM Lightify Dimming Switch..."){
            input(name: "switch1", type: "capability.button", title: "Which switch?", required: true)
        }
        section("Select device to turn on/off") {
            input(name: "target1", type: "capability.switch", title: "Which Target?", multiple: false, required: true)
            input(name: "isDimmable", title: "Is Dimmable Device?", type: "bool", required: false, defaultValue: true, submitOnChange: true)
        }

        if(isDimmable != null) {
            if(isDimmable) {
            	section("Set level for button 1 hold..."){
                	input(name: "upLevel", type: "number", range: "10..90", title: "Button 1 level?",  required: true)
              	}
              	section("Set level for button 2 hold..."){
					input(name: "downLevel", type: "number", range: "10..90", title: "Button 2 level?",  required: true)
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
  subscribe(switch1, "button.pushed", buttonPushedHandler)
  subscribe(switch1, "button.held", buttonHeldHandler)
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

    if(isDimmable != null && isDimmable) {
        if (buttonNumber == 1) {
            log.debug "Button 1 held (Setting brightness to $upLevel)"
            targets.setLevel(upLevel)
        } else {
            log.debug "Button 2 held (Setting brightness to $downLevel)"
            targets.setLevel(downLevel)
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