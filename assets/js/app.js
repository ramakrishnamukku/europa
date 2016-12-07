import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, browserHistory } from 'react-router'
import Layout from './pages/Layout'

import Overview from './pages/Overview'

import Registries from './pages/Registries'
import AddRegistryPage from './pages/AddRegistryPage'
import Repositories from './pages/Repositories'
import AddRepo from './pages/AddRepo'
import Settings from './pages/settings'

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
          <Route component={AddRegistryPage} path="/new-registry" />
          <Route component={Repositories} path="/repositories" />
          <Route component={AddRepo} path="/new-repository" />
          <Route component={Settings} path="/settings" />
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
