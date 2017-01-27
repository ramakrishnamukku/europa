/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import Reducers from './../reducers/AddRepoReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'
import {
  notifState,
  isAddNotificationValid
} from './NotificationActions'

import {
  newRegistryState
} from './RegistryActions'

// *************************************************
// General Repo Actions
// *************************************************

export function listRepos() {
  return new Promise((resolve, reject) => {
    this.setState({
      reposXHR: (this.state.repos.length) ? false : true
    }, () => {
      RAjax.GET.call(this, 'ListContainerRepos', {})
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
    selectExistingCredentialsDropdown: false,
    selectRepoDropdown: false,
    reposInRegistryXHR: false,
    reposInRegistry: [],
    reposInRegistryQuery: '',
    newRepo: {
      repo: {
        credId: '',
        name: ''
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
    if (keyPath == 'repo/credId') listReposForRegistry.call(this);
  })
};

export function resetCurrentRepoSearch() {
  return new Promise((resolve, reject) => {
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
        reposInRegistry: [],
        reposInRegistryQuery: '',
        newRepo: {
          repo: {
            ...this.state.addRepo.newRepo.repo,
            name: ''
          }
        }
      })
    }, () => resolve());
  });
}

export function listReposForRegistry() {
  let credId = NPECheck(this.state.addRepo, 'newRepo/repo/credId', null);
  let registry = (this.state.addRepo.newRepoCredsType == 'EXISTING') ? this.state.registriesMap[credId] : this.state.addRegistry.newRegistry


  if (registry) {
    this.setState({
      addRepo: GA.modifyProperty(this.state.addRepo, {
        reposInRegistryXHR: true,
        reposInRegistryQuery: '',
      })
    }, () => {
      RAjax.POST.call(this, 'ListReposInRegistry', {}, credId ? {
          credId
        } : registry)
        .then((res) => {
          this.setState({
            addRepo: GA.modifyProperty(this.state.addRepo, {
              reposInRegistry: res,
              reposInRegistryXHR: false,
            })
          });
        })
        .catch((err) => {
          this.setState({
            addRepo: GA.modifyProperty(this.state.addRepo, {
              reposInRegistry: [],
              errorMsg: 'Unable to list repositories for selected registry',
              reposInRegistryXHR: false,
            })
          });
        });
    });
  }
}

export function updateReposInRegisterQuery(e, eIsValue) {
  let value = (eIsValue) ? e : e.target.value;

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
      ...addRepoState.call(this),
      newRepoCredsType: type,
    }),
    addRegistry: GA.modifyProperty(this.state.addRegistry, {
      errorFields: [],
      errorMsg: '',
      newRegistry: newRegistryState.call(this)
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

export function addRepoRequest(afterAddCb) {
  if (!isAddRepoValid.call(this, true)) return;

  let postData = {
    repo: this.state.addRepo.newRepo.repo,
  };

  let notif = NPECheck(this.state, 'notif/newNotification', {});

  let shouldIncludeNotif = Object.keys(notif)
    .reduce((cur, next) => {
      cur = (!!notif[next]) ? cur + 1 : cur
      return cur;
    }, 0) > 1;

  if (shouldIncludeNotif) {
    if (!isAddNotificationValid.call(this)) return;

    postData.notification = notif;
  }

  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      XHR: true
    })
  }, () => {

    RAjax.POST.call(this, 'SaveContainerRepo', postData)
      .then((res) => {
        this.setState({
          addRepo: GA.modifyProperty(this.state.addRepo, {
            XHR: false,
            success: true,
          }),
          notif: notifState.call(this)
        }, () => {
          listRepos.call(this)

          if (afterAddCb) afterAddCb(res.id);
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


export function clearAddRepoError() {
  this.setState({
    addRepo: GA.modifyProperty(this.state.addRepo, {
      errorMsg: '',
      errorFields: [],
      validateOnInput: false
    })
  });
}

function isAddRepoValid(validateOnInput) {

  let required = {
    repo: {
      credId: 'Registry Provider',
      name: 'Docker Repository'
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
    eventsError: '',
    timelineSection: 'EVENTS',
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
    RAjax.POST.call(this, 'DeleteContainerRepo', {}, {
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

export function toggleActiveRepoSettings() {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      showSettings: !this.state.repoDetails.showSettings
    })
  })
}

export function setTimelineSection(section = ''){
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      timelineSection: section
    })
  });
}

export function listRepoEvents(repoId, skipXHR) {
  this.setState({
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      eventsXHR: (skipXHR) ? false : true
    })
  }, () => {
    RAjax.GET.call(this, 'ListRepoEvents', {
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
        console.error(err);
        let errorMsg = `There was an error retrieving your events for this repository. ${NPECheck(err, 'error/message', '')}`
        this.setState({
          repoDetails: GA.modifyProperty(this.state.repoDetails, {
            eventsXHR: false,
            eventsError: errorMsg
          })
        });
      })
  })
}

export function toggleEventDetails(eventId = null) {
  return new Promise((resolve, reject) => {
    this.setState({
      repoDetails: GA.modifyProperty(this.state.repoDetails, {
        activeEventId: (this.state.repoDetails.activeEventId == eventId) ? null : eventId
      })
    }, () => resolve(!!this.state.repoDetails.activeEventId));
  });
}