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
      errorFields: []
    })
  });
}

export function testNewNotification() {
  if (!isAddNotificationValid.call(this)) return;

  RAjax.POST.call(this, 'TestWebhookDelivery', {
      notification: this.state.notif.newNotification
    })
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

export function testExistingNotification(notification) {
  let id = notification.id;
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
        console.log(err);
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
          let errorMsg = `There was an error retrieving your notifications. ${NPECheck(err, 'error/message', '')}`
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              notifError: errorMsg,
              notifsXHR: false
            })
          }, () => reject());
        });
    });
  });
}

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
          let errorMsg = `There was an error redelivering your webhook. ${NPECheck(err, 'error/message', '')}`
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

export function deleteNotification(skipXHR) {
  return new Promise((resolve, reject) => {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        deleteNotificationXHR: (skipXHR) ? false : true
      })
    }, () => {
      RAjax.POST.call(this, 'DeleteRepoNotification', {}, {
          notificationId: this.state.notif.deleteNotifId
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
              notifError: errorMsg
            })
          }, () => reject());
        });
    });
  });
}

export function getEventNotificationRecords(recordIdsArray) {
  return new Promise((resolve, reject) => {
    this.setState({
      notif: GA.modifyProperty(this.state.notif, {
        notifRecordXHR: true
      })
    }, () => {
      let records = recordIdsArray.map(getNotificationRecord.bind(this));
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
          let errorMsg = `There was an error retrieving your notification records for this event. ${NPECheck(err, 'error/message', '')}`
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              retrieveNotifRecordsError: errorMsg,
              notifRecordXHR: false
            })
          });
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

export function getNotificationRecord(recordId) {
  return RAjax.GET.call(this, 'GetNotificationRecord', {
    notificationId: recordId
  });
}