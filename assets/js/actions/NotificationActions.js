import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Reducers from './../reducers/NotificationReducers'
import StatusCode from './../util/StatusCode'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'
import isValidScheme from './../util/isValidScheme'

// *************************************************
// Notification Actions
// *************************************************

export function notifState() {
  return {
    notifs: [],
    testNotification: {},
    testNotificationStatus: null,
    showNotificationTestResults: false,
    notifsXHR: false,
    notifError: '',
    errorFields: [],
    addNotifXHR: false,
    addNotifSuccess: null,
    deleteNotifId: '',
    deleteNotificationXHR: false,
    deleteNotificationError: '',
    notifRecordXHR: false,
    redeliverXHRID: false,
    redeliverError: '',
    retrieveNotifRecordsError: '',
    currentNotifRecords: [],
    testExistingNotification: {
      key: {
        XHR: false,
        status: '',
        responseCode: null,
        testNotification: {},
        displayWebhookData: false
      },
    },
    newNotification: {
      ...newNotificationState.call(this)
    }
  };
}

export function newNotificationState() {
  return {
    target: '',
    secret: '',
    type: 'WEBHOOK',
  };
}

export function resetNotifState() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, notifState.call(this))
  });
}

export function clearNotifError() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      notifError: '',
      errorFields: [],
      deleteNotificationError: ''
    })
  });
}

// Modify Permissions
export function testNewNotification() {
  if (!isAddNotificationValid.call(this)) return;
  let params = {}
  let repoId = NPECheck(this.state, 'repoDetails/activeRepo/id', '');

  if (repoId) {
    params = {
      repoId
    };
  }

  RAjax.POST.call(this, 'TestWebhookDelivery', {
      notification: this.state.notif.newNotification,
    }, params)
    .then((res) => {
      let responseCode = NPECheck(res, 'response/httpStatusCode', null);
      let testNotificationStatus = StatusCode(responseCode);
      this.setState({
        notif: GA.modifyProperty(this.state.notif, {
          testNotification: res,
          testNotificationStatus
        })
      })
    })
    .catch((err) => {
      console.error(err);
      let errorMsg = `There was an error testing your notification. ${NPECheck(err, 'error/message', '')}`
      this.setState({
        notif: GA.modifyProperty(this.state.notif, {
          notifError: errorMsg,
          notifsXHR: false
        })
      });
    });
}

// Modify Permissions
export function testExistingNotification(notification) {
  let id = notification.id;
  let repoId = NPECheck(this.state, 'repoDetails/activeRepo/id', '');

  this.setState({
    notif: Reducers(this.state.notif, {
      type: 'TOGGLE_EXISTING_NOTIFICATION_TEST_XHR',
      data: {
        id
      }
    })
  }, () => {
    RAjax.POST.call(this, 'TestWebhookDelivery', {
        notification
      }, {
        repoId
      })
      .then((res) => {
        let responseCode = NPECheck(res, 'response/httpStatusCode', null);
        let status = StatusCode(responseCode);
        this.setState({
          notif: Reducers(this.state.notif, {
            type: 'SET_EXISTING_NOTIFICATION_TEST_INFO',
            data: {
              id,
              status,
              responseCode,
              testNotification: res
            }
          })
        });
      })
      .catch((err) => {
        console.error(err);
        let errorMsg = `${NPECheck(err, 'error/message', 'There was an error testing your notification.')}`
        this.setState({
          notif: GA.modifyProperty(this.state.notif, {
            deleteNotificationError: errorMsg,
            notifsXHR: false,
            testExistingNotification: {}
          })
        });
      });
  });
}

export function toggleShowExistingNotificationTestResults(notifId) {
  let id = notifId;
  this.setState({
    notif: Reducers(this.state.notif, {
      type: 'TOGGLE_SHOW_EXISTING_NOTIFICATION_TEST_RESULTS',
      data: {
        id
      }
    })
  });
}

export function resetTestNotification() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      testNotification: {},
      testNotificationStatus: null,
    })
  });
}

export function toggleShowNotificationTestResults() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      showNotificationTestResults: !this.state.notif.showNotificationTestResults
    })
  });
}

// Read Permissions
export function listRepoNotifications(repoId, skipXHR) {
  return new Promise((resolve, reject) => {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        notifsXHR: (skipXHR) ? false : true
      })
    }, () => {
      RAjax.GET.call(this, 'ListRepoNotifications', {
          repoId
        })
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              notifs: res,
              notifsXHR: false
            })
          }, () => resolve());
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `${NPECheck(err, 'error/message', '')}`
          if (errorMsg == 'You do not have access to this operation') {
            this.setState({
              reposXHR: false,
              repoDetails: GA.modifyProperty(this.state.repoDetails, {
                isBlocked: true
              }),
              notif: GA.modifyProperty(this.state.notif, {
                notifError: errorMsg,
                notifsXHR: false
              })
            }, () => reject());
          } else {
            this.setState({
              notif: GA.modifyProperty(this.state.notif, {
                notifError: errorMsg,
                notifsXHR: false
              })
            }, () => reject());
          }
        });
    });
  });
}

// Modify Permissions
export function redeliverNotification(recordId) {
  return new Promise((resolve, reject) => {
    let repoId = NPECheck(this.state, 'repoDetails/activeRepo/id', '');
    let eventId = NPECheck(this.state, 'repoDetails/activeEventId', '');

    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        redeliverXHRID: recordId
      })
    }, () => {
      RAjax.POST.call(this, 'RedeliverWebhook', {}, {
          notificationId: recordId,
          repoId,
          eventId
        })
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              redeliverXHRID: false
            })
          }, () => resolve(res));
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `${NPECheck(err, 'error/message', '')}`
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              redeliverError: errorMsg
            })
          }, () => reject(res));
        });
    });
  });
}

export function clearRedeliverError() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      redeliverError: '',
      redeliverXHRID: false
    })
  });
}

export function updateNewNotificationField(prop, e, eIsValue) {
  let value = (eIsValue) ? e : e.target.value;
  this.setState({
    notif: Reducers(this.state.notif, {
      type: 'UPDATE_NEW_NOTIFICATION',
      data: {
        key: prop,
        value: value
      }
    })
  }, () => {
    if (prop == 'target' && NPECheck(this.state, 'notif/testNotificationStatus', false)) {
      resetTestNotification.call(this);
    }
  });
}

// Modify Permissions
export function addRepoNotification(skipXHR) {
  return new Promise((resolve, reject) => {

    if (!isAddNotificationValid.call(this)) return;

    let params = {
      repoId: NPECheck(this.state, 'repoDetails/activeRepo/id', null),
    };

    let postData = {
      notification: NPECheck(this.state, 'notif/newNotification', {})
    };

    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        addNotifXHR: (skipXHR) ? false : true
      })
    }, () => {
      RAjax.POST.call(this, 'SaveRepoNotification', postData, params)
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              addNotifXHR: false,
              addNotifSuccess: true,
              newNotification: newNotificationState.call(this),
              testNotification: {},
              testNotificationStatus: null,
              showNotificationTestResults: false,
              notifError: ''
            })
          }, () => resolve());
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `There was an error adding your notification. ${NPECheck(err, 'error/message', '')}`
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              addNotifXHR: false,
              addNotifSuccess: false,
              notifError: errorMsg
            })
          }, () => {
            reject();
          });
        });
    })
  });
}

export function isAddNotificationValid() {
  let notif = this.state.notif.newNotification;
  let isValid = isValidScheme(notif.target);
  if (isValid) {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        notifError: '',
        errorFields: []
      })
    });

    return true;

  } else {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        notifError: 'Target URL must start with http:// or https://',
        errorFields: ['target']
      })
    });

    return false;
  }
}


export function toggleRepoNotificationForDelete(notifId = null) {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      deleteNotifId: (notifId == this.state.notif.deleteNotifId) ? '' : notifId
    })
  });
}

// Modify Permissions
export function deleteNotification(skipXHR) {
  return new Promise((resolve, reject) => {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        deleteNotificationXHR: (skipXHR) ? false : true
      })
    }, () => {

      let repoId = NPECheck(this.state, 'repoDetails/activeRepo/id', '');

      RAjax.POST.call(this, 'DeleteRepoNotification', {}, {
          notificationId: this.state.notif.deleteNotifId,
          repoId: repoId
        })
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              deleteNotifId: '',
              deleteNotificationXHR: false
            })
          }, () => resolve());
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `There was an error deleting your notification. ${NPECheck(err, 'error/message', '')}`
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              deleteNotificationXHR: false,
              deleteNotificationError: errorMsg
            })
          }, () => reject());
        });
    });
  });
}

// Read Permissions
export function getEventNotificationRecords(recordIdsArray) {
  return new Promise((resolve, reject) => {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        notifRecordXHR: true
      })
    }, () => {
      let repoId = NPECheck(this.state, 'repoDetails/activeRepo/id', '');
      let records = recordIdsArray.map((recordId) => getNotificationRecord.call(this, recordId, repoId));
      Promise.all(records)
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              currentNotifRecords: res,
              notifRecordXHR: false
            })
          }, () => resolve());
        })
        .catch((err) => {
          console.error(err);
          let errorMsg = `${NPECheck(err, 'error/message', '')}`
          if (errorMsg == 'You do not have access to this operation') {
            this.setState({
              notif: GA.modifyProperty(this.state.notif, {
                retrieveNotifRecordsError: errorMsg,
                notifRecordXHR: false
              }),
              repoDetails: GA.modifyProperty(this.state.repoDetails, {
                isBlocked: true
              })
            }, () => reject());
          } else {
            this.setState({
              notif: GA.modifyProperty(this.state.notif, {
                retrieveNotifRecordsError: errorMsg,
                notifRecordXHR: false
              })
            }, () => reject());
          }
        });
    });
  });
}

export function clearNotifRecordsError() {
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      retrieveNotifRecordsError: '',

    }),
    repoDetails: GA.modifyProperty(this.state.repoDetails, {
      activeEventId: null
    })
  });
}

export function appendNotificationRecord(newRecord) {
  let currentRecords = NPECheck(this.state, 'notif/currentNotifRecords', []);
  let newRecords = [...currentRecords, newRecord];
  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      currentNotifRecords: newRecords
    })
  });
}

export function getNotificationRecord(recordId, repoId) {
  return RAjax.GET.call(this, 'GetNotificationRecord', {
    notificationId: recordId,
    repoId: repoId
  });
}