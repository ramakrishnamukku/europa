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

export function pipelinesState() {
  return {
    pipelines: null,
  }
}

export function singlePipelineState() {
  return {
    pipeline: null,
  }
}

export function listPipelines() {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelineXHR: this.state.pipelines.length ? false : true
    }, () => {
      RAjax.GET('ListPipelines')
      .then(res => {
        debugger

        // this.setState({
        //   pipelines: res,
        //   pipelineXHR: false
        // }, () => resolve() );

      })
      .catch(err => {
        this.setState({
          pipelineXHR: false
        }, () => reject() );
      });
    });
  });
}