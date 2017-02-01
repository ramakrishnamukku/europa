import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, IndexRoute, browserHistory } from 'react-router'
import Layout from './pages/Layout'
import LandingPage from './pages/LandingPage'
import Repositories from './pages/Repositories'
import RepoDetailsPage from './pages/RepoDetailsPage'
import CreateLocalRepo from './pages/CreateLocalRepo'
import AddRepo from './pages/AddRepo'
import Pipelines from './pages/Pipelines'
import NewPipeline from './pages/NewPipeline'
import Pipeline from './pages/Pipeline'
import Settings from './pages/Settings'
import StorageSettings from './components/StorageSettings'
import NotFound from './pages/NotFound'

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...PAGE_PROPS
    };
  }
  render() {
    let europa = (
      <Router history={browserHistory}>
        <Route component={Layout}>
          <Route component={LandingPage} path="/" />
          <Route component={Repositories} path="/repositories" />
          <Route component={RepoDetailsPage} path="/repository/:repoId" />
          <Route component={AddRepo} path="/new-repository" />
          <Route component={CreateLocalRepo} path="/create-repository" />
          <Route component={NewPipeline} path="/new-pipeline" />
          <Route component={Pipelines} path="/pipelines" />
          <Route component={Pipeline} path="/pipelines/:pipelineId" />
          <Route component={Settings} path="/settings" />
          <Route component={NotFound} path="*" />
        </Route>
      </Router>
    );

    let storageApp = (
      <Router history={browserHistory}>
        <Route component={Layout}>
          <Route component={StorageSettings} path="/" />
          <Route component={NotFound} path="*" />
        </Route>
      </Router>
    );

    return (this.state.hasOwnProperty('storage') && this.state.storage == false) ? storageApp : europa;
  }
}

window.MyApp = {
  init: function (opts) {
    var mountPoint = opts.mount;
    var config = opts.props;
    ReactDOM.render(React.createFactory(App)(config), document.getElementById(mountPoint));
  }
};
