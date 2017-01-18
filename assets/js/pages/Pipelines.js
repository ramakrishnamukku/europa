import React, {Component, PropTypes} from 'react'
import Btn from './../components/Btn'
import Loader from './../components/Loader'

export default class Pipelines extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  componentDidMount() {
    this.context.actions.listPipelines();
  }
  render() {
    if (this.props.pipelineXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
            Pipelines
          </h2>
        </div>
      </div>
    );
  }
}

Pipelines.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Pipelines.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};
