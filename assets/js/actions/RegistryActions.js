import * as GA from './../reducers/GeneralReducers'
import * as RAjax from './../util/RAjax'
import Validate from './../util/Validate'

export function registriesState() {
  return {
    registrySelectedForDelete: null,
    deleteRegistryXHR: false
  }
}

export function setRegistryForDelete(registry = null) {
  this.setState({
    registry: GA.modifyProperty(this.state.registry, {
      registrySelectedForDelete: registry
    })
  });
}

export function deleteRegistry() {
  console.log(this.state.registry.registrySelectedForDelete);

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
          console.log(this.state.registry.registrySelectedForDelete);
          console.log(res);
        })
      })
      .catch((err) => {
        this.setState({
          registry: GA.modifyProperty(this.state.registry, {
            deleteRegistryXHR: false
          })
        }, () => {
          console.log(err);
        })
      });
  })
}