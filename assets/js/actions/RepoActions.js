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
    success: null,
    XHR: false,
    testNotification: {},
    showNotificationTestResults: false,
    newRepo: {
      repo: {
        provider: '',
        region: '',
        credName: '',
        name: ''
      },
      notification: {
        target: '',
        type: 'WEBHOOK',
        secret: ''
      }
    }
  }
}

export function resetAddRepoState() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, addRepoState.call(this))
  })
}

export function listRepos() {
  this.setState({
    reposXHR: (this.state.repos.length) ? false : true
  }, () => {
    RAjax.GET('ListContainerRepos', {})
      .then((res) => {
        this.setState({
          repos: res,
          reposXHR: false
        })
      })
      .catch((err) => {
        this.setState({
          reposXHR: false
        });
      });
  });
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
  }, () => (this.state.addRepo.validateOnInput) ? isAddRepoValid.call(this, true) : null );
};

export function setNewRepoCredsType(type) {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      newRepoCredsType: type
    })
  });
}

export function selectCredsForNewRepo(e) {
  let creds = JSON.parse(e.target.value);

  delete creds['created']
  delete creds['secret']
  delete creds['key']

  creds['credName'] = creds['name'];
  delete creds[name];

  updateNewRepoField.call(this, 'repo', creds, true);
}


export function testNotification(){
  RAjax.POST('TestWebhookDelivery', this.state.addRepo.newRepo)
  .then((res) => {
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
        testNotification: res
      })
    })
  })
  .catch((err) => {
    console.error('Webhook Req failed');
    console.error(err);
  });
}

export function toggleShowNotification(){
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      showNotificationTestResults: !this.state.addRepo.showNotificationTestResults
    })
  });
}

export function addRepoRequest() {
  if (!isAddRepoValid.call(this, true)) return;

  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      XHR: true
    })
  }, () => {
    RAjax.POST('SaveContainerRepo', this.state.addRepo.newRepo)
      .then((res) => {
        this.setState({
          addRepo: GA.modifyProperty(this.state.addRepo, {
            XHR: false,
            success: true
          })
        })
      })
      .catch((err) => {
        let errorMsg = `There was an error adding your repository: ${err.error.message}`
        this.setState({
          addRepo: GA.modifyProperty(this.state.addRepo, {
            XHR: false,
            success: false,
            errorMsg,
          })
        })
      });
  });
}

export function clearAddRepoSuccess() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      success: null
    })
  });
}

export function canAddRepo() {
  return this.state.addRepo.errorMsg == '' && this.state.addRegistry.errorFields.length == 0;
}

function isAddRepoValid(validateOnInput) {
  let required = {
    repo: {
      provider: 'Repo Provider',
      region: 'Region',
      name: 'Docker Image Repository',
      credName: 'Credentials'
    },
    notification: {
      target: 'Webhook Target',
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