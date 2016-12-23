/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRegistryReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'

export function addRegistryState() {
  return {
    showModal: false,
    isEdit: false,
    errorMsg: '',
    errorFields: [],
    validateOnInput: false,
    success: null,
    XHR: false,
    newRegistry: {
      name: '',
      provider: '',
      region: '',
      key: '',
      secret: ''
    },
  }
}

export function resetAddRegistryState(){
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
  }, () => (this.state.addRegistry.validateOnInput) ? isAddRegistryValid.call(this, true) : null);
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
      RAjax.POST('SaveRegistryCreds', this.state.addRegistry.newRegistry)
        .then((res) => {
          this.setState({
            addRegistry: GA.modifyProperty(this.state.addRegistry, {
              success: true,
              XHR: false
            })
          }, () => resolve(res.id));
        })
        .catch((err) => {
          let errorMsg = `There was an error adding your registry: ${err.error.message}`
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

export function toggleShowAddEditRegistryModal() {
  return new Promise((resolve, reject) => {
    this.setState({
      addRegistry: GA.modifyProperty(this.state.addRegistry, {
          showModal: !this.state.addRegistry.showModal
      })
    }, () => resolve() )
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
    }, () => resolve());
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

function isAddRegistryValid(validateOnInput) {
  let required = {
    provider: 'Registry Provider',
    region: 'Region',
    key: 'Public Key',
    secret: 'Secret Key',
    name: 'Key Name'
  };

  let errorFields = Validate.call(this, this.state.addRegistry.newRegistry, required);

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