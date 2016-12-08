import Reducers from './../reducers/AddRepoReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'

export function addRepoState() {
  return {
    errorMsg: '',
    errorFields: [],
    validateOnInput: false,
    newRepoCredsType: 'EXISTING',
    newRepo: {
    	repo: {
    		provider: '',
    		region: '',
        credName: '',
    		name: ''
    	},
    	notification: {
  	    target: '',
  	    type:  'webhoook',
  		  secret: ''
    	}
    }
  }
}

export function resetAddRepoState(){
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, addRepoState.call(this))
  })
}

export function updateNewRepoField(keyPath, e, eIsValue = false) {
  let value = (eIsValue) ? e : e.target.value;

  this.setState({
    addRepo: Reducers(this.state.addRepo, {
      type: 'UPDATE_NEW_REPO',
      data: {
        keyPath,
        value
      }
    })
  });
};

export function setNewRepoCredsType(type){
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      newRepoCredsType: type
    })
  });
}

export function selectCredsForNewRepo(e){
  let creds = JSON.parse(e.target.value);
  
  delete creds['created']
  delete creds['secret']
  delete creds['key']
  
  creds['credName'] = creds['name'];
  delete creds[name];

  updateNewRepoField.call(this, 'repo', creds, true);
}

export function addRepoRequest() {
  if (!isAddRepoValid.call(this, true)) return;

  RAjax.POST('SaveContainerRepo', this.state.addRepo.newRepo)
    .then((res) => {
    	console.log(res);
    })
    .catch((err) => {
      let errorMsg = `There was an error adding your repository: ${err.error.message}`
      this.setState({
        addRepo: GA.modifyProperty(this.state.addRepo, {
          errorMsg
        })
      })
    });
}

export function canAddRepo(){
	return this.state.addRepo.errorMsg == '' && this.state.addRegistry.errorFields.length == 0;
}

function isAddRepoValid(validateOnInput) {
  let required = {
  	repo: {
  		provider: '',
  		region: '',
  		name: ''
  	},
  	notification: {
  		target: 'Target',
  		type: 'Type',
  	}
  };

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