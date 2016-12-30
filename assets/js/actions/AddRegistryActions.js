/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRegistryReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'
import { listReposForRegistry } from './RepoActions'

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
    newRegistry: {
      name: '',
      provider: '',
      region: '',
      key: '',
      secret: ''
    },
  }
}

export function resetAddRegistryState() {
  this.setState({
    addRegistry: GA.modifyProperty(this.state.addRegistry, addRegistryState.call(this))
  });
}

export function updateNewRegistryField(prop, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;

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
    if(isAddRegistryValid.call(this, true, !this.state.addRegistry.validateOnInput)) {
      listReposForRegistry.call(this)
    }
  });
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

      let url = this.state.addRegistry.credentialType == 'SERVICE_CREDENTIAL' ? 'SaveGcrServiceAccountCreds' : 'SaveRegistryCreds';

      RAjax.POST(url, this.state.addRegistry.newRegistry)
        .then((res) => {
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              success: true,
              XHR: false
            })
          }, () => resolve(res.id));
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

export function updateServiceAccountCredential(json){
  changeCredentialType.call(this, 'SERVICE_CREDENTIAL')
  .then(() => updateNewRegistryField.call(this, 'secret', json, true));
}

export function cancelServiceAccountCredentialUpload(){
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
    }, () => resolve() );
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
      RAjax.GET('GetRegionsForProvider', {
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
        newRegistry: reg
      })
    }, () =>{ 
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

  let required = {
    provider: 'Registry Provider',
    region: 'Region',
    key: 'Public Key',
    secret: 'Secret Key',
    name: 'Key Name'
  };

  if(this.state.addRegistry.credentialType == 'SERVICE_CREDENTIAL') {
    delete required.key;
    delete required.secret;
    delete required.name;
  }

  let errorFields = Validate.call(this, this.state.addRegistry.newRegistry, required);

  if(skipSetState) {
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