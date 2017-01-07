/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRegistryReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'

// *************************************************
// General Settings Actions
// *************************************************

export function settingsState() {
  return {
    section: 'CREDENTIALS',
    tokens: tokensState.call(this)
  }
}

export function resetSettingsState() {
  this.setState({
    settings: GA.modifyProperty(this.state.settings, settingsState.call(this))
  });
}

export function setSettingsSection(section) {
  this.setState({
    settings: GA.modifyProperty(this.state.settings, {
      section
    })
  });
}

// *************************************************
// API Token Actions
// *************************************************

export function tokensState() {
  return {
    showingTokens: [],
    selectedTokenForDelete: null
  };
}

export function resetTokensState() {
  this.setState({
    settings: GA.modifyProperty(this.state.settings, {
      tokens: tokensState.call(this)
    })
  });
}

export function toggleShowingToken(tokenString) {
  let showingTokens = NPECheck(this.state, 'settings/tokens/showingTokens', []);
  let isActive = showingTokens.includes(tokenString);

  if (isActive) {
    showingTokens = showingTokens.filter(tokenFilter => !(tokenFilter == tokenString))
  } else {
    showingTokens = [...showingTokens, tokenString];
  }

  this.setState({
    settings: Reducers(this.state.settings, {
      type: 'UPDATE_TOKENS_STATE',
      data: {
        showingTokens
      }
    })
  })
}

export function toggleTokenForDelete(tokenString = null) {
  this.setState({
    settings: Reducers(this.state.settings, {
      type: 'UPDATE_TOKENS_STATE',
      data: {
        selectedTokenForDelete: (tokenString == NPECheck(this.state, 'settings/tokens/selectedTokenForDelete', null)) ? null : tokenString
      }
    })
  });
}