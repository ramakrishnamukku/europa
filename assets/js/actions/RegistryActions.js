/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import Reducers from './../reducers/AddRegistryReducers'
import NPECheck from './../util/NPECheck'
import {
  listReposForRegistry,
  resetCurrentRepoSearch
} from './RepoActions'


// *************************************************
// General Registry Actions
// *************************************************

export function registryState() {
  return {
    isBlocked: false,
    registrySelectedForDelete: null,
    registriesError: '',
    deleteRegistryXHR: false,
    deleteRegistryErrorMsg: ''
  }
}

export function resetRegistryState() {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, registryState.call(this))
  });
}

export function listRegistries() {
  this.setState({
    registriesXHR: (this.state.registries.length) ? false : true
  }, () => {
    RAjax.GET.call(this, 'ListRegistryCreds', {})
      .then((res) => {

        let registriesMap = res.reduce((cur, repo) => {
          cur[repo.id] = repo
          return cur;
        }, {});

        this.setState({
          registries: res,
          registriesMap: registriesMap,
          registriesXHR: false,
          registry: GA.modifyProperty(this.state.registry, {
            isBlocked: false,
            registriesError: ''
          })
        });

      })
      .catch((err) => {
        let errorMsg = `${err.error.message}`;
        if (errorMsg == 'You do not have access to this operation') {
          this.setState({
            registries: [],
            registriesMap: {},
            registriesXHR: false,
            registry: GA.modifyProperty(this.state.registry, {
              isBlocked: true,
              registriesError: 'You are not allowed to list registry credentials.'
            })
          });
        } else {
          this.setState({
            registriesXHR: false,
            registry: GA.modifyProperty(this.state.registry, {
              isBlocked: false,
              registriesError: errorMsg
            })
          });
        }
      });
  });
};

export function setRegistryForDelete(registry = null) {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, {
      registrySelectedForDelete: registry,
      deleteRegistryErrorMsg: ''
    })
  });
}

export function deleteRegistry() {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, {
      deleteRegistryXHR: true
    })
  }, () => {
    RAjax.POST.call(this, 'DeleteRegistryCreds', {}, this.state.registry.registrySelectedForDelete)
      .then((res) => {
        this.setState({
          registry: GA.modifyProperty(this.state.registry, {
            deleteRegistryXHR: false
          })
        }, () => {
          listRegistries.call(this);
        });
      })
      .catch((err) => {
        let errorMsg = `Failed to delete registry credentials: ${err.error.message}`;
        this.setState({
          registry: GA.modifyProperty(this.state.registry, {
            deleteRegistryXHR: false,
            deleteRegistryErrorMsg: errorMsg
          })
        });
      });
  })
}

// *************************************************
// Add Registry Actions
// *************************************************

export function addRegistryState() {
  return {
    showModal: false,
    isEdit: false,
    errorMsg: '',
    errorFields: [],
    validateOnInput: false,
    success: null,
    XHR: false,
    selectProviderDropdown: false,
    selectRegionDropdown: false,
    providerRegions: [],
    credentialType: 'KEY_CREDENTIAL',
    newRegistry: newRegistryState.call(this)
  }
}

export function resetAddRegistryState() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, addRegistryState.call(this))
  });
}

export function newRegistryState() {
  return {
    name: '',
    provider: '',
    region: '',
    key: '',
    secret: '',
    endpoint: ''
  };
}

export function resetNewRegistryState() {
  return new Promise((resolve) => {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        newRegistry: newRegistryState.call(this)
      })
    }, () => resolve());
  });
}

export function clearAddRegistryError() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, {
      errorMsg: '',
      errorFields: [],
      validateOnInput: false
    })
  });
}

export function updateNewRegistryField(prop, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;
  Promise.resolve()
    .then((resolve, reject) => {
      return (prop == 'provider') ? resetNewRegistryState.call(this) : null
    })
    .then(() => {
      return (prop == 'provider') ? resetCurrentRepoSearch.call(this) : null
    })
    .then(() => {
      this.setState({
        addRegistry: Reducers(this.state.addRegistry, {
          type: 'UPDATE_NEW_REGISTRY',
          data: {
            prop,
            value
          }
        })
      }, () => {
        if (prop == 'provider') getRegionsForProvider.call(this);
        if (isAddRegistryValid.call(this, true, !this.state.addRegistry.validateOnInput)) {
          listReposForRegistry.call(this)
        }
      });
    })
};

export function addRegistryRequest() {
  return new Promise((resolve, reject) => {
    if (!isAddRegistryValid.call(this, true)) {
      reject();
      return;
    }

    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        XHR: true
      })
    }, () => {

      let url = 'SaveRegistryCreds';

      if (this.state.addRegistry.credentialType == 'SERVICE_CREDENTIAL') {
        url = 'SaveGcrServiceAccountCreds'
      }

      RAjax.POST.call(this, url, this.state.addRegistry.newRegistry)
        .then((res) => {
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              success: true,
              XHR: false
            })
          }, () => {
            listRegistries.call(this);
            resolve(res.id)
          });
        })
        .catch((err) => {
          let errorMsg = `There was an error adding your registry: ${NPECheck(err, 'error/message', 'Unknown')}`
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              errorMsg,
              success: false,
              XHR: false
            })
          }, () => reject());
        });
    });

  });
};

export function updateServiceAccountCredential(json) {
  changeCredentialType.call(this, 'SERVICE_CREDENTIAL')
    .then(() => updateNewRegistryField.call(this, 'secret', json, true));
}

export function cancelServiceAccountCredentialUpload() {
  changeCredentialType.call(this, 'KEY_CREDENTIAL')
    .then(() => {
      this.setState({
        addRegistry: GA.modifyProperty(this.state.addRegistry, {
          newRegistry: GA.modifyProperty(NPECheck(this, 'state/addRegistry/newRegistry'), {
            secret: ''
          })
        })
      });
    });
}

export function changeCredentialType(credentialType) {
  return new Promise((resolve, reject) => {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        credentialType
      })
    }, () => resolve());
  });
}

export function toggleSelectRegionDropdown() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, {
      selectRegionDropdown: !this.state.addRegistry.selectRegionDropdown
    })
  });
}

export function toggleSelectProviderDropdown() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, {
      selectProviderDropdown: !this.state.addRegistry.selectProviderDropdown
    })
  });
}

export function toggleShowAddEditRegistryModal() {
  return new Promise((resolve, reject) => {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        showModal: !this.state.addRegistry.showModal
      })
    }, () => resolve())
  });
}


export function getRegionsForProvider() {
  return new Promise((resolve, reject) => {
    let provider = NPECheck(this.state.addRegistry, 'newRegistry/provider', null);

    if (provider) {
      RAjax.GET.call(this, 'GetRegionsForProvider', {
          provider
        })
        .then((res) => {
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              providerRegions: res
            })
          }, () => resolve());
        })
        .catch((err) => {
          let errorMsg = `There was an error retreiving the available regions for the selected provider: ${NPECheck(err, 'error/message', 'Unknown')}`
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              errorMsg,
            })
          }, () => reject());
        });
    }
  });
}

export function setRegistryForEdit(reg) {
  return new Promise((resolve, reject) => {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        showModal: true,
        isEdit: true,
        newRegistry: reg,
        credentialType: reg.provider == 'GCR' ? 'SERVICE_CREDENTIAL' : 'KEY_CREDENTIAL'
      })
    }, () => {
      getRegionsForProvider.call(this);
      resolve();
    });
  });
}

export function clearAddRegistrySuccess() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, {
      success: null
    })
  });
}

export function canAddRegistry() {
  return this.state.addRegistry.errorMsg == '' && this.state.addRegistry.errorFields.length == 0;
}

function isAddRegistryValid(validateOnInput, skipSetState) {
  let provider = 'Registry Provider';
  let region = 'Region';
  let key = 'Public Key';
  let secret = 'Secret Key';
  let name = 'Key Name';
  let username = 'Username';
  let password = 'Password';
  let endpoint = 'Endpoint';

  let required = {
    provider,
    region,
    key,
    secret,
    name,
  };

  let requiredLengths = {};

  let currentProvider = NPECheck(this.state, 'addRegistry/newRegistry/provider', '');

  switch (currentProvider) {
    case 'ECR':
      required = {
        provider,
        region,
        key,
        secret,
        name,
      };
      break;

    case 'GCR':
      required = {
        provider,
        region,
        name,
        secret
      };

      break;

    case 'DOCKERHUB':

      required = {
        provider,
        username,
        password,
        name,
      };

      requiredLengths = {
        provider: 0,
        username: 0,
        password: 7,
        name: 0,
      };

      break;

    case 'PRIVATE':

      required = {
        provider,
        username,
        password,
        name,
        endpoint
      };

      break;
  }

  let errorFields = Validate.call(this, this.state.addRegistry.newRegistry, required, "", requiredLengths);

  if (skipSetState) {
    return (errorFields.names.length) ? false : true
  }

  if (errorFields.names.length) {
    let errorMsg = `Missing required fields: ${errorFields.names.join(', ')}`;
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        errorMsg,
        validateOnInput,
        errorFields: errorFields.keys,
      })
    });
    return false
  } else {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
        errorMsg: '',
        errorFields: []
      })
    });
    return true
  }
};