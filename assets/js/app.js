import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, browserHistory } from 'react-router'
import Layout from './pages/Layout'
import AddRegistry from './pages/AddRegistry'

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }
  render() {
    return (
      <Router component={Layout} history={browserHistory}>
        <Route component={AddRegistry} path="/new"/>
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
