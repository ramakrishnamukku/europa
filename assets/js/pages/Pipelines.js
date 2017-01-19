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
  renderNoPipelines() {
    return (
      <div className="ContentContainer">
        <div className="NoContent">
          <h3>
            You have no Pipelines
          </h3>
          <Btn className="LargeBlueButton"
             onClick={() => console.log("todo")}
             text="Add Pipeline"
             canClick={true} />
        </div>
      </div>
    );
  }
  renderNewPipeline() {
    if (this.props.pipelinesStore.initNewPipeline) {
      return (
        <div>TODO, the list</div>
      );
    }
  }
  renderPipelineList() {
    if (!this.props.pipelinesStore.initNewPipeline) {
      if (this.props.pipelinesStore.pipelines.length == 0) {
        return this.renderNoPipelines();
      } else {
        return (
          <div>
            {this.props.pipelinesStore.pipelines.map((pipeline, idx) => {
              return (
                <div>{pipeline.containerRepoId}</div>
              );
            }, this)}
          </div>
        );
      }
    }
  }
  render() {
    // console.log(this.props)
    if (this.props.pipelinesStore.pipelinesXHR) {
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
        {this.renderNewPipeline()}
        {this.renderPipelineList()}
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
