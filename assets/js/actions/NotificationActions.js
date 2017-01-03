import Reducers from './../reducers/NotificationReducers'
import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'
import NPECheck from './../util/NPECheck'

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
    notifsError: '',
    addNotifXHR: false,
    addNotifSuccess: null,
    deleteNotifId: '',
    deleteNotificationXHR: false,
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
              notifsError: errorMsg,
              notifsXHR: false
            })
          }, () => reject());
        });
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
              newNotification: newNotificationState.call(this)
            })
          }, () => resolve());
        })
        .catch((err) => {
          console.error(err);
          this.setState({
            notif: GA.modifyProperty(this.state.notif, {
              addNotifXHR: false,
              addNotifSuccess: false
            })
          }, () => {
            reject();
          });
        });
    })
  });
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
              notifsError: errorMsg
            })
          }, () => reject());
        });
    });
  });
}