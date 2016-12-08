import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'


export function registryState() {
  return {
    registrySelectedForDelete: null,
    deleteRegistryXHR: false,
    deleteRegistryErrorMsg: ''
  }
}

export function resetRegistryState() {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, registryState.call(this))
  });
}

export function listRegistries() {
  this.setState({
    registriesXHR: (this.state.registries.length) ? false : true
  }, () => {
    RAjax.GET('ListRegistryCreds', {})
      .then((res) => {
        this.setState({
          registries: res,
          registriesXHR: false
        })
      })
      .catch((err) => {
        this.setState({
          registriesXHR: false
        });
      });
  });
};

export function setRegistryForDelete(registry = null) {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, {
      registrySelectedForDelete: registry,
      deleteRegistryErrorMsg: ''
    })
  });
}

export function deleteRegistry() {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, {
      deleteRegistryXHR: true
    })
  }, () => {
    RAjax.GET('DeleteRegistryCreds', this.state.registry.registrySelectedForDelete)
      .then((res) => {
        this.setState({
          registry: GA.modifyProperty(this.state.registry, {
            deleteRegistryXHR: false
          })
        }, () => {
          listRegistries.call(this);
        });
      })
      .catch((err) => {

        let errorMsg = `Failed to delete registry credentials: ${err.error.message}`;
        this.setState({
          registry: GA.modifyProperty(this.state.registry, {
            deleteRegistryXHR: false,
            deleteRegistryErrorMsg: errorMsg
          })
        });

      });
  })
}