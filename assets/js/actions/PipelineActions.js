import Reducers from './../reducers/AddRepoReducers'
import * as GR from './../reducers/GeneralReducers'
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
    initNewPipeline: false,
    newPipelineTemplate: {
      name: null,
    },
    pipelines: [],
    // XHR
    pipelinesXHR: false,
    newPipelineXHR: false,
    removePipelineXHR: false,
    addPipelineComponentXHR: false,
    movePipelineComponentXHR: false,
    removePipelineComponentXHR: false,
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
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        pipelinesXHR: true,
      })
    }, () => {
      RAjax.GET('ListPipelines')
      .then(res => {
        console.log(res)
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            pipelines: res,
            pipelinesXHR: false,
          })
        }, () => resolve() );
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            pipelinesXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function createPipeline(postData) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        newPipelineXHR: true,
      })
    }, () => {
      RAjax.POST('NewPipeline', {}, postData)
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            newPipelineXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function removePipeline(postData) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        removePipelineXHR: true,
      })
    }, () => {
      RAjax.POST('RemovePipeline', {}, postData)
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            removePipelineXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function addPipelineComponent(postData) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        addPipelineComponentXHR: true,
      })
    }, () => {
      RAjax.POST('AddPipelineComponent', {}, postData)
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            addPipelineComponentXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function movePipelineComponent(postData) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        movePipelineComponentXHR: true,
      })
    }, () => {
      RAjax.POST('MovePipelineComponent', {}, postData)
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            movePipelineComponentXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function removePipelineComponent(postData) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        removePipelineComponentXHR: true,
      })
    }, () => {
      RAjax.POST('RemovePipelineComponent', {}, postData)
      .then(res => {
        // TODO
      })
      .catch(err => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            removePipelineComponentXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}