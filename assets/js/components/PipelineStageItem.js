import React, { Component, PropTypes } from 'react'

export default class PipelineStageItem extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deleteToggled: false
    };
  }
  renderTrigger() {
    if (this.props.empty) return;
    // Don't render for the last stage
    if (this.props.pipeline.components.length - 1 == this.props.idx) return;

    return (
      <div className="stage-trigger">
        <div className="stage-trigger-pipe">
          <img src={ this.props.stage.triggerOn
                     ? "/_assets/images/dis-pipeline-green.svg"
                     : "/_assets/images/dis-pipeline-yellow.svg" } />
        </div>
        <div className="stage-trigger-toggle">
          <div className="stage-trigger-toggle-check">
            <i className={this.props.stage.triggerOn
                          ? "icon-dis-box-check cursor-on-hover"
                          : "icon-dis-box-uncheck cursor-on-hover"}
               onClick={this.toggleAutoDeployTrigger} />
            <span>Auto Push</span>
          </div>
        </div>
      </div>
    );
  }
  renderEmptyOption() {
    if (!NPECheck(this.props.stage, 'autoDeployTrigger', null)) {
      return <option value="">Select...</option>
    }
  }
  renderStageDetails() {
    if (this.state.deleteToggled) {
      return (
        <div>are you sure?</div>
      );
    }

    return (
      <div className="stage-destination">
        <div className="stage-dest-interior">
          <div className="stage-dest-details">
            <div style={ {position: "relative", top: "2px"} }>
              <span style={{color: "#3a73e1", fontSize: "1.3rem", fontWeight: "bold"}}>
                {dest.deploymentName}
              </span>
            </div>
            <div>
              <div className="meta-details">
                <strong>Cluster:</strong>
                <span>Foo</span>
              </div>
            </div>
            <div className="delete-stage">
              <i className="icon-dis-close"
                 onClick={() => this.setState({deleteToggled: !this.state.deleteToggled})} />
            </div>
          </div>
        </div>
      </div>
    );
  }

  renderEmptyStage() {
    return (
      <div className="pipeline-stage-item">
        <div className="pipeline-grey-wrap">
          <div className="stage-destination-wrap">
            <div className="left-icon-col"
                 style={ {background: "#808285"} }>
              <i className="icon-dis-repo" />
            </div>
            <div className="stage-destination">
              <div className="stage-dest-interior">
                <div className="stage-dest-details">
                  <span className="stage-not-connected">You have not connected a repository</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  render() {
    if (this.props.empty) {
      return this.renderEmptyStage();
    }

    return (
      <div className="pipeline-stage-item">
        <div className="pipeline-grey-wrap">
          <div className="stage-destination-wrap">
            <div className="left-icon-col">
              <i className="icon-dis-repo" />
            </div>
            <div className="stage-destinations">
              {this.renderStageDetails()}
            </div>
          </div>
        </div>
        {this.renderTrigger()}
      </div>
    );
  }
}

PipelineStageItem.propTypes = {
  empty: PropTypes.bool,
};
