import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, browserHistory } from 'react-router'
import Layout from './pages/Layout'
import Overview from './pages/Overview'
import AddRegistry from './pages/AddRegistry'
import AddRepository from './pages/AddRepository'
import Registries from './pages/Registries'

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  render() {
    return (
      <Router  history={browserHistory}>
        <Route component={Layout} >
          <Route component={Overview} path="/" />
          <Route component={Registries} path="/registries" />
          <Route component={AddRegistry} path="/new-registry" />
          <Route component={AddRepository} path="/new-repository" />
        </Route>
      </Router>
    );
  }
}

window.MyApp = {
  init: function (opts) {
    var mountPoint = opts.mount;
    var config = opts.props;
    ReactDOM.render(React.createFactory(App)(config), document.getElementById(mountPoint));
  }
};
