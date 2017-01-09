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
    allTokens: [],
    tokensXHR: false,
    deleteTokenXHR: false,
    statusXHR: false,
    showingTokens: [],
    selectedTokenForStatusUpdate: null,
    selectedTokenForDelete: null,
    tokenItemError: null,
    tokenPageError: null
  };
}

export function resetTokensState() {
  this.setState({
    settings: GA.modifyProperty(this.state.settings, {
      tokens: tokensState.call(this)
    })
  });
}

export function listAuthTokens() {
  this.setState({
    settings: Reducers(this.state.settings, {
      type: 'UPDATE_TOKENS_STATE',
      data: {
        tokensXHR: !NPECheck(this.state, 'settings/tokens/allTokens/length', false)
      }
    })
  }, () => {
    RAjax.GET('ListAuthTokens', {})
      .then((res) => {
        this.setState({
          settings: Reducers(this.state.settings, {
            type: 'UPDATE_TOKENS_STATE',
            data: {
              tokensXHR: false,
              allTokens: res
            }
          })
        });
      })
      .catch((err) => {
        console.error(err);
        let errorMsg = `There was an error listing your API Tokens: ${err.error.message}`;
        this.setState({
          settings: Reducers(this.state.settings, {
            type: 'UPDATE_TOKENS_STATE',
            data: {
              tokensXHR: false,
              tokenPageError: errorMsg
            }
          })
        });
      })
  });
}

export function createAuthToken() {
  return new Promise((resolve, reject) => {
    RAjax.POST('CreateAuthToken')
      .then((res) => {
        resolve(res);
      })
      .catch((err) => {
        console.error(err);
        let errorMsg = `There was an error creating your API token: ${err.error.message}`;
        this.setState({
          settings: Reducers(this.state.settings, {
            type: 'UPDATE_TOKENS_STATE',
            data: {
              tokenItemError: errorMsg
            }
          })
        }, () => reject(err));
      });
  });
}

export function deleteAuthToken() {
  return new Promise((resolve, reject) => {
    this.setState({
      settings: Reducers(this.state.settings, {
        type: 'UPDATE_TOKENS_STATE',
        data: {
          deleteTokenXHR: true,
          selectedTokenForStatusUpdate: null
        }
      })
    }, () => {
      let token = NPECheck(this.state, 'settings/tokens/selectedTokenForDelete', '');

      RAjax.POST('DeleteAuthToken', {}, {
          token
        })
        .then((res) => {
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                deleteTokenXHR: false,
                selectedTokenForDelete: null
              }
            })
          }, () => resolve(res));
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `There was an error deleting your API token: ${err.error.message}`;
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                deleteTokenXHR: false,
                tokenItemError: errorMsg
              }
            })
          }, () => reject(err));
        });
    });
  });
}


export function setAuthTokenStatus(tokenString, status) {
  return new Promise((resolve, reject) => {
    this.setState({
      settings: Reducers(this.state.settings, {
        type: 'UPDATE_TOKENS_STATE',
        data: {
          statusXHR: true,
          selectedTokenForStatusUpdate: tokenString,
          selectedTokenForDelete: null,
        }
      })
    }, () => {
      RAjax.POST('SetAuthTokenStatus', {
          token: tokenString,
          status
        })
        .then((res) => {
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                statusXHR: false,
                selectedTokenForStatusUpdate: null
              }
            })
          }, () => resolve(res));
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `There was an error updating your API token status: ${err.error.message}`;
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                statusXHR: false,
                tokenItemError: errorMsg
              }
            })
          }, () => reject(err));
        });
    });
  });
}

export function clearTokenItemError() {
  return new Promise((resolve, reject) => {
    this.setState({
      settings: Reducers(this.state.settings, {
        type: 'UPDATE_TOKENS_STATE',
        data: {
          tokenPageError: null,
          tokenItemError: null,
          selectedTokenForDelete: null,
          selectedTokenForStatusUpdate: null
        }
      })
    }, () => resolve());
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