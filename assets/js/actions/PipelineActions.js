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
        this.setState({
          pipelines: res,
          pipelineXHR: false
        }, () => resolve() );
      })
      .catch(err => {
        this.setState({
          pipelineXHR: false
        }, () => reject() );
      });
    });
  });
}

export function createPipeline() {
  return new Promise((resolve, reject) => {
    this.setState({
      newPipelineXHR: true
    }, () => {
      RAjax.GET('NewPipeline')
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          newPipelineXHR: false
        }, () => reject() );
      });
    });
  });
}

export function removePipeline() {
  return new Promise((resolve, reject) => {
    this.setState({
      removePipelineXHR: true
    }, () => {
      RAjax.GET('RemovePipeline')
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          removePipelineXHR: false
        }, () => reject() );
      });
    });
  });
}

export function addPipelineComponent() {
  return new Promise((resolve, reject) => {
    this.setState({
      addPipelineComponentXHR: true
    }, () => {
      RAjax.GET('AddPipelineComponent')
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          addPipelineComponentXHR: false
        }, () => reject() );
      });
    });
  });
}

export function movePipelineComponent() {
  return new Promise((resolve, reject) => {
    this.setState({
      movePipelineComponentXHR: true
    }, () => {
      RAjax.GET('MovePipelineComponent')
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          movePipelineComponentXHR: false
        }, () => reject() );
      });
    });
  });
}

export function removePipelineComponent() {
  return new Promise((resolve, reject) => {
    this.setState({
      removePipelineComponentXHR: true
    }, () => {
      RAjax.GET('RemovePipelineComponent')
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          removePipelineComponentXHR: false
        }, () => reject() );
      });
    });
  });
}