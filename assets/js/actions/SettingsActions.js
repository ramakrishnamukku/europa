/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRegistryReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import NPECheck from './../util/NPECheck'
import { updateUrlParams } from './../util/UrlManager'

// *************************************************
// General Settings Actions
// *************************************************

export function settingsState() {
  return {
    section: 'creds',
    tokens: tokensState.call(this),
    storage: storageState.call(this)
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
  }, () => updateUrlParams({ section }));
}


// *************************************************
// API Token Actions
// *************************************************

export function tokensState() {
  return {
    allTokens: [],
    tokensXHR: false,
    createTokenXHR: false,
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
    RAjax.GET.call(this, 'ListAuthTokens', {})
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

    this.setState({
      settings: Reducers(this.state.settings, {
        type: 'UPDATE_TOKENS_STATE',
        data: {
          createTokenXHR: true
        }
      })
    }, () => {
      RAjax.POST.call(this, 'CreateAuthToken')
        .then((res) => {
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                createTokenXHR: false
              }
            })
          }, () => resolve(res));
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `There was an error creating your API token: ${err.error.message}`;
          this.setState({
            settings: Reducers(this.state.settings, {
              type: 'UPDATE_TOKENS_STATE',
              data: {
                tokenItemError: errorMsg,
                createTokenXHR: false
              }
            })
          }, () => reject(err));
        });
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

      RAjax.POST.call(this, 'DeleteAuthToken', {}, {
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
      RAjax.POST.call(this, 'SetAuthTokenStatus', {
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

// *************************************************
// API Token Actions
// *************************************************

export function storageState() {
  return {
    storageCreds: storageCredsState.call(this),
    saveStorageSuccess: false,
    getXHR: false,
    getError: '',
    saveStorageXHR: false,
    error: '',
    regionDropDownIsOpen: false,
    regions: [],
    regionsError: '',
  };
}

export function storageCredsState() {
  return {
    osType: 'S3',
    osBucket: '',
    osEndpoint: '',
    osCredKey: '',
    osCredSecret: '',
    osPathPrefix: '',
    osDiskRoot: ''
  };
}

export function resetStorageState() {
  this.setState({
    settings: GA.modifyProperty(this.state.settings, {
      storage: storageState.call(this)
    })
  });
}

export function clearStorageError() {
  this.setState({
    settings: {
      ...this.state.settings,
      storage: {
        ...this.state.settings.storage,
        error: '',
        getError: ''
      }
    }
  })
}

export function getStorageSettings() {
  return new Promise((resolve, reject) => {
    this.setState({
      settings: GA.modifyProperty(this.state.settings, {
        ...this.state.settings,
        storage: {
          ...this.state.settings.storage,
          getXHR: true
        }
      })
    }, () => {

      RAjax.GET.call(this, 'GetStorageSettings', {})
        .then((res) => {
          this.setState({
            settings: GA.modifyProperty(this.state.settings, {
              ...this.state.settings,
              storage: {
                ...this.state.settings.storage,
                getXHR: false,
                storageCreds: res
              }
            })
          });
        })
        .catch((err) => {
        console.error(err);
        let errorMsg = NPECheck(err, 'error/message', 'Please try again or contact support');
        let error = `There was an error retrieving your storage settings: ${errorMsg}`
          this.setState({
            settings: GA.modifyProperty(this.state.settings, {
              ...this.state.settings,
              storage: {
                ...this.state.settings.storage,
                getXHR: false,
                getError: error
              }
            })
          });
        })
    });
  });
}

export function updateStorageCreds(prop, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;
  let additionalState = {};
  if (prop == 'osEndpoint') value = `s3://${value}`;
  if (prop == 'osType') {
    additionalState = storageCredsState.call(this);
  }


  this.setState({
    settings: {
      ...this.state.settings,
      storage: Reducers(this.state.settings.storage, {
        type: 'UPDATE_STORAGE_CREDS',
        data: {
          ...additionalState,
          [prop]: value
        }
      })
    }
  });
}

export function toggleSelectRegionForStorageCredentialsDropDown() {
  this.setState({
    settings: {
      ...this.state.settings,
      storage: GA.modifyProperty(this.state.settings.storage, {
        regionDropDownIsOpen: !NPECheck(this.state, 'settings/storage/regionDropDownIsOpen', true)
      })
    }
  });
}

export function listRegionsForStorageCredentials() {
  return new Promise((resolve, reject) => {
    RAjax.GET.call(this, 'GetRegionsForProvider', {
        provider: 'ECR'
      })
      .then((res) => {
        this.setState({
          settings: {
            ...this.state.settings,
            storage: GA.modifyProperty(this.state.settings.storage, {
              regions: res
            })
          }
        }, () => resolve())
      })
      .catch((err) => {
        console.error(err);
        reject();
      })
  });
}

export function saveStorageSettings() {
  return new Promise((resolve, reject) => {
    let storageSettings = NPECheck(this.state, 'settings/storage/storageCreds', {});

    this.setState({
      settings: {
        ...this.state.settings,
        storage: GA.modifyProperty(this.state.settings.storage, {
          saveStorageXHR: true,
          error: '',
        })
      }
    }, () => {

      let op = 'UpdateStorageCreds';
      let path = undefined;
      
      // Is intial save?
      if(this.state.hasOwnProperty('storage') && this.state.storage == false) {
        op = 'SaveStorageSettings';
        path = '/storage'
      } 

      RAjax.POST.call(this, op, storageSettings, {}, path)
        .then((res) => {
          this.setState({
            settings: {
              ...this.state.settings,
              storage: GA.modifyProperty(this.state.settings.storage, {
                saveStorageXHR: false,
                saveStorageSuccess: true
              })
            }
          }, () => {
            resolve();
          });
        })
        .catch((err) => {
          console.error(err);
          let error = NPECheck(err, 'error/message', 'There was an error saving your credentials.');
          this.setState({
            settings: {
              ...this.state.settings,
              storage: GA.modifyProperty(this.state.settings.storage, {
                saveStorageXHR: false,
                error: error,
                saveStorageSuccess: false
              })
            }
          }, () => reject());
        });
    });

  });
}