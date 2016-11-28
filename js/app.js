import React, {Component} from 'react'
import ReactDOM from 'react-dom'

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }
  render() {
    return (
        <div className="index">
          <h1>Hello World!</h1>
        </div>
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
