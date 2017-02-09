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
    isBlocked: false,
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

    // XHR Error
    newPipelineXHRError: null,
  }
}

export function singlePipelineState() {
  return {
    isBlocked: false,
    pipeline: null,
    repoConnectTemplate: null,
    section: null,

    // XHR
    getPipelineXHR: false,
    removePipelineXHR: false,
    setContainerRepoXHR: false,
    addPipelineComponentXHR: false,
    movePipelineComponentXHR: false,
    removePipelineComponentXHR: false,
    removePipelineMainStageXHR: false,

    // XHR Error
    setContainerRepoXHRError: null,
    addPipelineComponentXHRError: null,
    movePipelineComponentXHRError: null,
    removePipelineComponentXHRError: null,
    removePipelineMainStageXHRError: null,
  }
}

export function clearPipelinesXHRErrors() {
  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
      newPipelineXHRError: null,
    })
  })
}

export function clearPipelineXHRErrors() {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
      newPipelineXHRError: null,
      setContainerRepoXHRError: null,
      addPipelineComponentXHRError: null,
      movePipelineComponentXHRError: null,
      removePipelineComponentXHRError: null,
    })
  })
}

export function resetSinglePipelineState() {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, singlePipelineState.call(this))
  });
}

export function resetPipelinesState() {
  this.setState({
    pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, pipelinesState.call(this))
  });
}

export function setPipelinePageSection(section) {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
      section: section
    })
  })
}

export function updateRepoConnect(repo) {
  this.setState({
    pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
      repoConnectTemplate: repo
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

// Read Permissions
export function listPipelines() {
  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        pipelinesXHR: true,
      })
    }, () => {
      RAjax.GET.call(this, 'ListPipelines')
        .then(res => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              isBlocked: false,
              pipelines: res,
              pipelinesXHR: false,
            })
          }, () => resolve());
        })
        .catch(err => {
          let errorMsg = NPEChceck(err, 'error/message', 'There was an error loading your pipelines');
          if (errorMsg == 'You do not have access to this operation') {
            this.setState({
              pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
                isBlocked: true,
                pipelinesXHR: false,
              })
            }, () => reject());
          } else {
            this.setState({
              pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
                isBlocked: false,
                pipelinesXHR: false,
              })
            }, () => reject());
          }
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
      RAjax.GET.call(this, 'GetPipeline', {
          pipelineId
        })
        .then(res => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              isBlocked: false,
              pipeline: res,
              getPipelineXHR: false,
            })
          }, () => resolve(res));
        })
        .catch(err => {
          let errorMsg = NPEChceck(err, 'error/message', 'There was an error loading your pipeline');
          if (errorMsg == 'You do not have access to this operation') {
            this.setState({
              pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
                isBlocked: true,
                getPipelineXHR: false,
              })
            }, () => reject());
          } else {
            this.setState({
              pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
                isBlocked: false,
                getPipelineXHR: false,
              })
            }, () => reject());
          }
        });
    });
  });
}

export function setContainerRepo() {
  const postData = {
    pipelineId: NPECheck(this.state.pipelineStore, 'pipeline/id', null),
    containerRepoId: NPECheck(this.state.pipelineStore, 'repoConnectTemplate/id', null)
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
        setContainerRepoXHR: true,
        setContainerRepoXHRError: false,
      })
    }, () => {
      RAjax.POST.call(this, 'SetPipelineContainerRepoId', {}, postData)
        .then(res => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              pipeline: res,
              setContainerRepoXHR: false,
              setContainerRepoXHRError: false,
              repoConnectTemplate: null,
              section: null
            })
          }, () => resolve());
        })
        .catch(err => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              setContainerRepoXHR: false,
              setContainerRepoXHRError: NPECheck(err, 'error/message', "")
            })
          }, () => reject());
        });
    });
  });
}

export function createPipeline() {
  let newPipeline = {...this.state.pipelinesStore.newPipelineTemplate
  }

  // Remove validation if clean
  delete newPipeline["errorFields"];

  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        newPipelineXHR: true,
      })
    }, () => {
      RAjax.POST.call(this, 'NewPipeline', {}, newPipeline)
        .then(res => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              pipelines: res,
              newPipelineXHR: false,
              newPipelineXHRError: false,
              newPipelineTemplate: pipelinesState()["newPipelineTemplate"],
              initNewPipeline: false
            })
          }, () => resolve());
        })
        .catch(err => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              newPipelineXHR: false,
              newPipelineXHRError: NPECheck(err, 'error/message', "")
            })
          });
        });
    });
  });
}

export function removePipeline() {
  const postData = {
    pipelineId: this.state.pipelineStore.pipeline.id
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
        removePipelineXHR: true,
      })
    }, () => {
      RAjax.POST.call(this, 'RemovePipeline', {}, postData)
        .then(res => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              pipelines: res,
              removePipelineXHRError: false,
              removePipelineXHR: false,
            })
          }, () => {
            this.context.router.push('/pipelines')
            resetSinglePipelineState.call(this)
          });
        })
        .catch(err => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              removePipelineXHR: false,
              removePipelineXHRError: NPECheck(err, 'error/message', "")
            })
          }, () => reject());
        });
    });
  });
}

export function addPipelineComponent() {
  const postData = {
    type: "CopyToRepository",
    pipelineId: this.state.pipelineStore.pipeline.id,
  }
  const content = {
    destinationContainerRepoDomain: NPECheck(this.state.pipelineStore, 'repoConnectTemplate/domain', null),
    destinationContainerRepoId: NPECheck(this.state.pipelineStore, 'repoConnectTemplate/id', null),
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
        addPipelineComponentXHR: true,
      })
    }, () => {
      RAjax.POST.call(this, 'AddPipelineComponent', content, postData)
        .then(res => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              pipeline: res,
              addPipelineComponentXHR: false,
              addPipelineComponentXHRError: false,
              section: null,
            })
          }, () => resolve(res));
        })
        .catch(err => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              addPipelineComponentXHR: false,
              addPipelineComponentXHRError: NPECheck(err, 'error/message', "")
            })
          });
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
      RAjax.POST.call(this, 'MovePipelineComponent', {}, postData)
        .then(res => {
          // TODO
          resolve();
        })
        .catch(err => {
          this.setState({
            pipelinesStore: GR.modifyProperty(this.state.pipelinesStore, {
              movePipelineComponentXHR: false,
            })
          });
        });
    });
  });
}

export function removePipelineComponent(pipelineComponentId) {
  const postData = {
    pipelineComponentId: pipelineComponentId,
    pipelineId: this.state.pipelineStore.pipeline.id
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
        removePipelineComponentXHR: pipelineComponentId,
      })
    }, () => {
      RAjax.POST.call(this, 'RemovePipelineComponent', {}, postData)
        .then(res => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              pipeline: res,
              removePipelineComponentXHR: false,
            })
          }, () => resolve(res));
        })
        .catch(err => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              removePipelineComponentXHR: false,
              removePipelineComponentXHRError: NPECheck(err, 'error/message', "")
            })
          });
        });
    });
  });
}

export function removeMainPipelineStage() {
  const postData = {
    pipelineId: NPECheck(this.state.pipelineStore, 'pipeline/id', null),
  }

  return new Promise((resolve, reject) => {
    this.setState({
      pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
        removePipelineMainStageXHR: true,
      })
    }, () => {
      RAjax.POST.call(this, 'DeletePipelineContainerRepoId', {}, postData)
        .then(res => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              pipeline: res,
              removePipelineMainStageXHR: false,
            })
          }, () => resolve(res));
        })
        .catch(err => {
          this.setState({
            pipelineStore: GR.modifyProperty(this.state.pipelineStore, {
              removePipelineMainStageXHR: false,
              removePipelineMainStageXHRError: NPECheck(err, 'error/message', "")
            })
          });
        });
    });
  });
}