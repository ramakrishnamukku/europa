import Reducers from './../reducers/NotificationReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
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
    currentNotifRecords: [],
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

export function testNotification() {
  RAjax.POST('TestWebhookDelivery', {
      notification: this.state.notif.newNotification
    })
    .then((res) => {
      let statusCode = NPECheck(res, 'response/httpStatusCode', null);
      let testNotificationStatus;

      if (200 <= statusCode && statusCode <= 299) testNotificationStatus = 'SUCCESS';
      if ((0 <= statusCode && statusCode <= 199) || (300 <= statusCode && statusCode <= 399)) testNotificationStatus = 'WARNING';
      if (400 <= statusCode) testNotificationStatus = 'ERROR';

      this.setState({
        notif: GA.modifyProperty(this.state.notif, {
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
      RAjax.GET('ListRepoNotifications', {
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
          let errorMsg = `There was an error retreiving your notifications. ${NPECheck(err, 'error/message', '')}`
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

    RAjax.POST('RedeliverWebhook', {}, {
      notificationId: recordId,
      repoId,
      eventId
    })
    .then((res) => {
      resolve(res);
    })
    .catch((err) => {
      reject(err);
    });
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
      RAjax.POST('SaveRepoNotification', postData, params)
        .then((res) => {
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              addNotifXHR: false,
              addNotifSuccess: true,
              newNotification: newNotificationState.call(this),
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
  if (isValid) return true;

  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      notifError: 'Target URL must start with http:// or https://',
      errorFields: ['target']
    })
  });

  return false;
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
      RAjax.POST('DeleteRepoNotification', {}, {
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
      let records = recordIdsArray.map(getNotificationRecord.bind(this))

      Promise.all(records)
        .then((res) => {

          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              currentNotifRecords: res,
              notifRecordXHR: false
            })
          }, () => resolve());
        });

    });
  });
}

export function appendNotificationRecord(newRecord){
  let currentRecords = NPECheck(this.state, 'notif/currentNotifRecords', []);

  let newRecords = [...currentRecords, newRecord];

  this.setState({
    notif: GA.modifyProperty(this.state.notif, {
      currentNotifRecords: newRecords
    })
  });
}

export function getNotificationRecord(recordId) {
  return new Promise((resolve, reject) => {
    RAjax.GET('GetNotificationRecord', {
        notificationId: recordId
      })
      .then((res) => {
        resolve(res);
      })
      .catch((err) => {
        resolve(err);
      });
  });
}