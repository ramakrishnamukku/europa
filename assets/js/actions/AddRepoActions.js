import Reducers from './../reducers/AddRepoReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'

export function addRepoState() {
  return {
    errorMsg: '',
    errorFields: [],
    validateOnInput: false,
    newRepo: {
      dockerImage: '',
      webhookUrl: '',
      secret: ''
    }
  }
}

export function updateNewRepoField(prop, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;

  this.setState({
    addRepo: Reducers(this.state.addRepo, {
      type: 'UPDATE_NEW_REPO',
      data: {
        prop,
        value
      }
    })
  });
};

export function addRepoRequest() {
  if (!isAddRepoValid.call(this, true)) return;

  // RAjax.POST('SaveRegistryCreds', this.state.addRegistry.newRegistry)
  //   .then((res) => {
  //     this.setState({
  //       addRegistry: GA.modifyProperty(this.state.addRegistry, addRegistryState())
  //     });
  //   })
  //   .catch((err) => {
  //     let errorMsg = `There was an error adding your registry: ${err.error.message}`
  //     this.setState({
  //       addRegistry: GA.modifyProperty(this.state.addRegistry, {
  //         errorMsg
  //       })
  //     })
  //   });
}

export function canAddRepo(){
	return this.state.addRepo.errorMsg == '' && this.state.addRegistry.errorFields.length == 0;
}

function isAddRepoValid(validateOnInput) {
  let required = {
    dockerImage: 'Docker Image Repository',
    webhookUrl: 'Webhook URL'
  }

  let errorFields = Validate.call(this, this.state.addRepo.newRepo, required);

  if (errorFields.names.length) {
    let errorMsg = `Missing required fields: ${errorFields.names.join(', ')}`;
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
        errorMsg,
        validateOnInput,
        errorFields: errorFields.keys,
      })
    });
    return false
  } else {
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
        errorMsg: '',
        errorFields: []
      })
    });
    return true
  }
}