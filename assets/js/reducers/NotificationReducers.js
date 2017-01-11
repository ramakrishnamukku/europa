/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import * as GA from './../reducers/GeneralReducers'
import CreateOrSetPropertyValue from './../util/CreateOrSetPropertyValue'
import NPECheck from './../util/NPECheck'

export default function NotificationReducers(state, action) {
  switch (action.type) {

    case 'UPDATE_NEW_NOTIFICATION':
      return updateNewNotification(state, action.data);

    case 'TOGGLE_EXISTING_NOTIFICATION_TEST_XHR':
      return toggleExistingNotificationTestXHR(state, action.data);

    case 'SET_EXISTING_NOTIFICATION_TEST_STATUS':
      return setExistingNotificationStatus(
        toggleExistingNotificationTestXHR(state, action.data),
        action.data
      );

    default:
      console.error(`No reducer found for action ${action.type}`);
      return state;
  }
}

function updateNewNotification(state, data) {
  let newNotification = state.newNotification;
  CreateOrSetPropertyValue(newNotification, data.key, data.value);

  return {
    ...state,
    newNotification
  };
}

function toggleExistingNotificationTestXHR(state, data) {
  let testExistingNotification = state.testExistingNotification;
  let id = data.id;
  let notifTest = testExistingNotification[id] || {};
  notifTest.XHR = !notifTest.XHR;
  testExistingNotification[id] = notifTest;

  return {
    ...state,
    testExistingNotification
  };

}

function setExistingNotificationStatus(state, data) {
  let testExistingNotification = state.testExistingNotification;
  let id = data.id;
  let notifTest = testExistingNotification[id] || {};
  notifTest.status = data.status
  testExistingNotification[id] = notifTest;
  
  return {
    ...state,
    testExistingNotification
  };
}