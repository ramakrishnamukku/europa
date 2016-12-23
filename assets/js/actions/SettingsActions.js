/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRegistryReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'

export function settingsState() {
  return {
    section: 'CREDENTIALS',
  }
}

export function resetSettingsState(){
  this.setState({
    settings: GA.modifyProperty(this.state.settings, settingsState.call(this))
  });
}

export function setSettingsSection(section){
  this.setState({
    settings: GA.modifyProperty(this.state.settings, {
      section
    })
  });
}