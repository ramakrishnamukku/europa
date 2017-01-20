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
      errorFields: []
    },
    pipelines: [],
    filteredPipelines: null,
    // XHR
    pipelinesXHR: false,
    newPipelineXHR: false,
    removePipelineXHR: false,
  }
}

export function singlePipelineState() {
  return {
    pipeline: null,
    initialRepoConnect: null,
    section: null,
    // XHR
    getPipelineXHR: false,
    removePipelineXHR: false,
    setContainerRepoXHR: false,
    addPipelineComponentXHR: false,
    movePipelineComponentXHR: false,
    removePipelineComponentXHR: false,
  }
}

export function setPipelinePageSection(section) {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
      section: section
    })
  })
}

export function updateInitialRepoConnect(repo) {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
      initialRepoConnect: repo
    })
  })
}

export function toggleInitNewPipeline() {
  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
      initNewPipeline: !this.state.pipelinesStore.initNewPipeline
    })
  }, () => {
    // Reset if the user closes the modal without creating
    if (!this.state.pipelinesStore.initNewPipeline) {
      resetNewPipelineTemplate.call(this);
    }
  })
}

export function resetNewPipelineTemplate() {
  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
      newPipelineTemplate: pipelinesState()["newPipelineTemplate"]
    })
  })
}

export function updateNewPipelineTemplate(field, value) {
  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
      newPipelineTemplate: {
        ...this.state.pipelinesStore.newPipelineTemplate,
        [field]: value
      }
    })
  })
}

export function filterPipelines(filterString) {
  let filteredPipelines = this.state.pipelinesStore.pipelines.slice(0).filter(pipeline => {
    return pipeline.name.indexOf(filterString) != -1;
  })

  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
      filteredPipelines: filteredPipelines
    })
  })
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

export function getPipeline(pipelineId) {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelineStore, {
        getPipelineXHR: true,
      })
    }, () => {
      RAjax.GET('GetPipeline', { pipelineId } )
      .then(res => {
        this.setState({
          pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
            pipeline: res,
            getPipelineXHR: false,
          })
        }, () => resolve(res) );
      })
      .catch(err => {
        this.setState({
          pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
            getPipelineXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function setContainerRepo() {
  const postData = {
    pipelineId: this.state.pipelineStore.pipeline.id,
    containerRepoId: this.state.pipelineStore.initialRepoConnect.id
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelineStore, {
        setContainerRepoXHR: true,
      })
    }, () => {
      RAjax.POST('SetPipelineContainerRepoId', {}, postData)
      .then(res => {
        this.setState({
          pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
            pipeline: res,
            setContainerRepoXHR: false,
            initialRepoConnect: null,
            section: null
          })
        }, () => resolve() );
      })
      .catch(err => {
        this.setState({
          pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
            setContainerRepoXHR: false,
          })
        }, () => reject() );
      });
    });
  });
}

export function createPipeline() {
  let newPipeline = { ...this.state.pipelinesStore.newPipelineTemplate }

  if (!newPipeline.name) {
    updateNewPipelineTemplate.apply(this, ["errorFields", ["name"] ]);
    return;
  }

  // Remove validation if clean
  delete newPipeline["errorFields"];

  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        newPipelineXHR: true,
      })
    }, () => {
      RAjax.POST('NewPipeline', {}, newPipeline)
      .then(res => {
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            pipelines: res,
            newPipelineXHR: false,
            newPipelineTemplate: pipelinesState()["newPipelineTemplate"],
            initNewPipeline: false
          })
        }, () => resolve() );
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
        this.setState({
          pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
            pipelines: res,
            removePipelineXHR: false,
          })
        }, () => resolve() );
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

export function addPipelineComponent(repo) {
  // TODO, prepare the postData object

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