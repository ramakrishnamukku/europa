/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRepoReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'

// *************************************************
// General Repo Actions
// *************************************************

export function listRepos() {
  return new Promise((resolve, reject) => {
    this.setState({
      reposXHR: (this.state.repos.length) ? false : true
    }, () => {
      RAjax.GET('ListContainerRepos', {})
        .then((res) => {

          let reposMap = res.reduce((cur, repo) => {
            cur[repo.id] = repo
            return cur;
          }, {});

          this.setState({
            repos: res,
            reposMap: reposMap,
            reposXHR: false
          }, () => resolve());
        })
        .catch((err) => {
          this.setState({
            reposXHR: false
          }, () => reject());
        });
    });
  });
}

export function filterRepos(e, eIsValue) {
  let value = (eIsValue) ? e : e.target.value;

  this.setState({
    reposFilterQuery: value
  });
}


// *************************************************
// Add Repo Actions
// *************************************************

export function addRepoState() {
  return {
    errorMsg: '',
    errorFields: [],
    validateOnInput: false,
    newRepoCredsType: 'EXISTING',
    success: null,
    XHR: false,
    testNotification: {},
    testNotificationStatus: null,
    showNotificationTestResults: false,
    selectExistingCredentialsDropdown: false,
    selectRepoDropdown: false,
    reposInRegistryXHR: false,
    reposInRegistry: [],
    reposInRegistryQuery: '',
    newRepo: {
      repo: {
        credId: '',
        name: ''
      },
      notification: {
        target: '',
        type: 'WEBHOOK',
        secret: ''
      }
    }
  };
}

export function resetAddRepoState() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, addRepoState.call(this))
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
  }, () => {
    if (this.state.addRepo.validateOnInput) isAddRepoValid.call(this, true);
    if(keyPath == 'repo/credId') listReposForRegistry.call(this);
  })
};


export function listReposForRegistry() {
  let credId = NPECheck(this.state.addRepo, 'newRepo/repo/credId', null);
  let registry = (this.state.addRepo.newRepoCredsType == 'EXISTING') ? this.state.registriesMap[credId] : this.state.addRegistry.newRegistry


  if (registry) {
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
          reposInRegistryXHR: true
      })
    }, () => {
      RAjax.GET('ListReposInRegistry', credId ? {
          credId
        } : registry)
        .then((res) => {
          this.setState({
            addRepo: GA.modifyProperty(this.state.addRepo, {
              reposInRegistry: res,
              reposInRegistryXHR: false
            })
          });
        })
        .catch((err) => {
          this.setState({
            addRepo: GA.modifyProperty(this.state.addRepo, {
              reposInRegistry: [],
              errorMsg: 'Unable to list repositories for selected registry',
              reposInRegistryXHR: false
            })
          });
        });
    });
  }
}

export function updateReposInRegisterQuery(e, eIsValue){
  let value = (eIsValue) ? e : e.target.value;

  console.log(value)

  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      reposInRegistryQuery: value
    })
  });
}

export function toggleSelectRepoDropdown() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      selectRepoDropdown: !this.state.addRepo.selectRepoDropdown
    })
  });
}

export function setNewRepoCredsType(type) {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      newRepoCredsType: type,
      reposInRegistryXHR: false,
      reposInRegistry: [],
      reposInRegistryQuery: ''
    })
  });
}

export function selectCredsForNewRepo(e, value) {
  let id;

  if (e) {
    id = JSON.parse(e.target.value).id
  } else if (value) {
    id = value
  }

  updateNewRepoField.call(this, 'repo/credId', id, true);
}

export function toggleSelectExistingCredsDropdown() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      selectExistingCredentialsDropdown: !this.state.addRepo.selectExistingCredentialsDropdown
    })
  });
}

export function testNotification() {
  RAjax.POST('TestWebhookDelivery', {
      notification: this.state.addRepo.newRepo.notification
    })
    .then((res) => {
      let statusCode = NPECheck(res, 'response/httpStatusCode', null);
      let testNotificationStatus;

      if (200 <= statusCode && statusCode <= 299) testNotificationStatus = 'SUCCESS';
      if ((0 <= statusCode && statusCode <= 199) || (300 <= statusCode && statusCode <= 399)) testNotificationStatus = 'WARNING';
      if (400 <= statusCode) testNotificationStatus = 'ERROR';

      this.setState({
        addRepo: GA.modifyProperty(this.state.addRepo, {
          testNotification: res,
          testNotificationStatus
        })
      })
    })
    .catch((err) => {
      console.error('Webhook Req failed');
      console.error(err);
    });
}

export function toggleShowNotificationTestResults() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      showNotificationTestResults: !this.state.addRepo.showNotificationTestResults
    })
  });
}

export function addRepoRequest(afterAddCb) {
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
        }, () => {
          listRepos.call(this)

          if (afterAddCb) afterAddCb();
        })
      })
      .catch((err) => {
        console.error(err);
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
      credId: 'Registry Provider',
      name: 'Docker Repository'
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

// *************************************************
// Repo Detail Actions
// *************************************************


export function repoDetailsState() {
  return {
    activeRepo: {},
    pageXHR: false,
    deleteXHR: false,
    isDeleting: false,
    showSettings: false,
    events: [],
    eventsXHR: false,
    activeEventId: null,
  };
}

export function resetRepoDetailsState() {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.addRepo, repoDetailsState.call(this))
  });
}

export function toggleRepoDetailsPageXHR() {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      pageXHR: !this.state.repoDetails.XHR
    })
  });
}

export function setActiveRepoDetails(repoId) {
  return new Promise((resolve, reject) => {
    let repo = this.state.reposMap[repoId];

    this.setState({
      repoDetails: GA.modifyProperty(this.state.repoDetails, {
        activeRepo: repo,
        pageXHR: false
      })
    }, () => resolve());
  });
}

export function toggleActiveRepoDelete() {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      isDeleting: !this.state.repoDetails.isDeleting
    })
  })
}

export function deleteActiveRepo(afterDeleteCb) {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      deleteXHR: true
    })
  }, () => {
    RAjax.GET('DeleteContainerRepo', {
        id: this.state.repoDetails.activeRepo.id
      })
      .then((res) => {
        this.setState({
          repoDetails: GA.modifyProperty(this.state.repoDetails, {
            deleteXHR: false
          })
        }, () => {
          if (afterDeleteCb) afterDeleteCb();
        });
      })
      .catch((err) => {
        this.setState({
          repoDetails: GA.modifyProperty(this.state.repoDetails, {
            deleteXHR: false
          })
        });
      });
  });
}

export function listRepoEvents(repoId) {

  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      eventsXHR: true
    })
  }, () => {

    RAjax.GET('ListRepoEvents', {
        repoId
      })
      .then((res) => {

        this.setState({
          repoDetails: GA.modifyProperty(this.state.repoDetails, {
            events: res,
            eventsXHR: false
          })
        });

      })
      .catch((err) => {

        this.setState({
          repoDetails: GA.modifyProperty(this.state.repoDetails, {
            eventsXHR: false
          })
        });

      })
  })
}

export function toggleActiveRepoSettings() {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      showSettings: !this.state.repoDetails.showSettings
    })
  })
}

export function toggleEventDetails(eventId = null) {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      activeEventId: (this.state.repoDetails.activeEventId == eventId) ? null : eventId
    })
  });
}